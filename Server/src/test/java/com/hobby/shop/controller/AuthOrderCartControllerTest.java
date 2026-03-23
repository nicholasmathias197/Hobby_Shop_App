package com.hobby.shop.controller;

import com.hobby.shop.dto.request.CancelOrderRequest;
import com.hobby.shop.dto.request.CartItemRequest;
import com.hobby.shop.dto.request.LoginRequest;
import com.hobby.shop.dto.request.OrderRequest;
import com.hobby.shop.dto.request.OrderStatusUpdateRequest;
import com.hobby.shop.dto.request.PaymentStatusUpdateRequest;
import com.hobby.shop.dto.request.RegisterRequest;
import com.hobby.shop.dto.request.TrackingNumberUpdateRequest;
import com.hobby.shop.dto.response.CartResponse;
import com.hobby.shop.dto.response.JwtResponse;
import com.hobby.shop.dto.response.OrderResponse;
import com.hobby.shop.dto.response.RegisterResponse;
import com.hobby.shop.exception.BadRequestException;
import com.hobby.shop.security.JwtUtils;
import com.hobby.shop.security.UserDetailsImpl;
import com.hobby.shop.service.AuthService;
import com.hobby.shop.service.CartService;
import com.hobby.shop.service.OrderService;
import com.hobby.shop.util.SecurityUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthOrderCartControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthService authService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private CartService cartService;

    @Mock
    private OrderService orderService;

    @Mock
    private HttpServletRequest servletRequest;

    @Mock
    private HttpServletResponse servletResponse;

    private AuthController authController;
    private OrderController orderController;
    private CartController cartController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authenticationManager, jwtUtils, authService, securityUtils, cartService);
        orderController = new OrderController(orderService, securityUtils);
        cartController = new CartController(cartService, securityUtils, servletRequest, servletResponse);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginMergesCartWhenSessionIdIsProvidedAsQueryParam() {
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L,
                "user@example.com",
                "secret",
                "Jane",
                "Doe",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");

        JwtResponse response = (JwtResponse) authController.login(request, "session-123", null).getBody();

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getEmail()).isEqualTo("user@example.com");
        assertThat(response.getRoles()).containsExactly("ROLE_USER");
        verify(cartService).mergeCarts("user@example.com", "session-123");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
    }

    @Test
    void loginUsesHeaderSessionAndIgnoresMergeFailures() {
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = new UserDetailsImpl(
                2L,
                "header@example.com",
                "secret",
                "John",
                "Smith",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        LoginRequest request = new LoginRequest();
        request.setEmail("header@example.com");
        request.setPassword("password");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("header-token");
        doThrow(new IllegalStateException("merge failed")).when(cartService).mergeCarts("header@example.com", "header-session");

        JwtResponse response = (JwtResponse) authController.login(request, null, "header-session").getBody();

        assertThat(response.getToken()).isEqualTo("header-token");
        assertThat(response.getRoles()).containsExactly("ROLE_ADMIN");
        verify(cartService).mergeCarts("header@example.com", "header-session");
    }

    @Test
    void registerReturnsCreatedAndSkipsMergeWhenNoSessionIsProvided() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        RegisterResponse response = mock(RegisterResponse.class);

        when(response.getId()).thenReturn(10L);
        when(authService.registerUser(request)).thenReturn(response);

        var entity = authController.register(request, null, null);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(entity.getBody()).isSameAs(response);
        verify(cartService, never()).mergeCarts(any(), any());
    }

    @Test
    void registerUsesHeaderSessionWhenPresent() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("merge@example.com");
        RegisterResponse response = mock(RegisterResponse.class);

        when(response.getId()).thenReturn(11L);
        when(authService.registerUser(request)).thenReturn(response);

        var entity = authController.register(request, "", "session-from-header");

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(cartService).mergeCarts("merge@example.com", "session-from-header");
    }

    @Test
    void orderControllerDelegatesAllEndpointsAndHandlesOptionalReason() {
        Pageable pageable = PageRequest.of(0, 10);
        OrderRequest orderRequest = mock(OrderRequest.class);
        OrderResponse order = mock(OrderResponse.class);
        Page<OrderResponse> page = new PageImpl<>(List.of(order));
        CancelOrderRequest cancelRequest = new CancelOrderRequest();
        cancelRequest.setReason("Changed my mind");
        OrderStatusUpdateRequest statusRequest = new OrderStatusUpdateRequest();
        statusRequest.setStatus("SHIPPED");
        statusRequest.setComment("Packed");
        PaymentStatusUpdateRequest paymentRequest = new PaymentStatusUpdateRequest();
        paymentRequest.setPaymentStatus("PAID");
        TrackingNumberUpdateRequest trackingRequest = new TrackingNumberUpdateRequest();
        trackingRequest.setTrackingNumber("TRACK-1");

        when(securityUtils.getCurrentUserEmail()).thenReturn("buyer@example.com");
        when(orderService.createOrder("buyer@example.com", orderRequest)).thenReturn(order);
        when(orderService.createGuestOrder("guest-session", orderRequest)).thenReturn(order);
        when(orderService.getUserOrders("buyer@example.com", pageable)).thenReturn(page);
        when(orderService.getOrderByNumber("buyer@example.com", "ORDER-1")).thenReturn(order);
        when(orderService.cancelOrder("buyer@example.com", 8L, "Changed my mind")).thenReturn(order);
        when(orderService.cancelOrder("buyer@example.com", 9L, null)).thenReturn(order);
        when(orderService.getGuestOrder("ORDER-2", "guest@example.com")).thenReturn(order);
        when(orderService.getAllOrders(pageable)).thenReturn(page);
        when(orderService.getOrdersByCustomer(5L, pageable)).thenReturn(page);
        when(orderService.getOrdersByStatus("PENDING", pageable)).thenReturn(page);
        when(orderService.getOrderById(7L)).thenReturn(order);
        when(orderService.updateOrderStatus(7L, "SHIPPED", "Packed")).thenReturn(order);
        when(orderService.updatePaymentStatus(7L, "PAID")).thenReturn(order);
        when(orderService.updateTrackingNumber(7L, "TRACK-1")).thenReturn(order);

        assertThat(orderController.createOrder(orderRequest).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(orderController.createGuestOrder(orderRequest, "guest-session", null).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(orderController.createGuestOrder(orderRequest, "", "guest-session").getBody()).isSameAs(order);
        assertThat(orderController.getUserOrders(pageable).getBody()).isSameAs(page);
        assertThat(orderController.getOrderByNumber("ORDER-1").getBody()).isSameAs(order);
        assertThat(orderController.cancelOrder(8L, cancelRequest).getBody()).isSameAs(order);
        assertThat(orderController.cancelOrder(9L, null).getBody()).isSameAs(order);
        assertThat(orderController.getGuestOrder("guest@example.com", "ORDER-2").getBody()).isSameAs(order);
        assertThat(orderController.getAllOrders(pageable).getBody()).isSameAs(page);
        assertThat(orderController.getOrdersByCustomer(5L, pageable).getBody()).isSameAs(page);
        assertThat(orderController.getOrdersByStatus("PENDING", pageable).getBody()).isSameAs(page);
        assertThat(orderController.getOrderById(7L).getBody()).isSameAs(order);
        assertThat(orderController.updateOrderStatus(7L, statusRequest).getBody()).isSameAs(order);
        assertThat(orderController.updatePaymentStatus(7L, paymentRequest).getBody()).isSameAs(order);
        assertThat(orderController.updateTrackingNumber(7L, trackingRequest).getBody()).isSameAs(order);
    }

    @Test
    void createGuestOrderThrowsWhenNoSessionIdIsProvided() {
        assertThatThrownBy(() -> orderController.createGuestOrder(mock(OrderRequest.class), null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Session ID is required for guest checkout");
    }

    @Test
    void cartControllerUsesAuthenticatedEndpointsWhenUserIsLoggedIn() {
        CartItemRequest itemRequest = new CartItemRequest();
        CartResponse cartResponse = new CartResponse();

        when(securityUtils.isAuthenticated()).thenReturn(true);
        when(securityUtils.getCurrentUserEmail()).thenReturn("cart@example.com");
        when(cartService.getCartByUser("cart@example.com")).thenReturn(cartResponse);
        when(cartService.addItemToUserCart("cart@example.com", itemRequest)).thenReturn(cartResponse);
        when(cartService.updateCartItemQuantity("cart@example.com", 2L, 4)).thenReturn(cartResponse);
        when(cartService.removeItemFromUserCart("cart@example.com", 3L)).thenReturn(cartResponse);
        when(cartService.getCartItemCount("cart@example.com", true)).thenReturn(7);
        when(cartService.mergeCarts("cart@example.com", "merge-session")).thenReturn(cartResponse);

        assertThat(cartController.getUserCart().getBody()).isSameAs(cartResponse);
        assertThat(cartController.addItemToCart(itemRequest).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(cartController.updateCartItemQuantity(2L, 4).getBody()).isSameAs(cartResponse);
        assertThat(cartController.removeItemFromCart(3L).getBody()).isSameAs(cartResponse);
        assertThat(cartController.clearCart().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(cartController.getCartItemCount().getBody()).isEqualTo(7);
        assertThat(cartController.mergeCarts("merge-session").getBody()).isSameAs(cartResponse);

        verify(cartService).clearUserCart("cart@example.com");
        verify(cartService).mergeCarts("cart@example.com", "merge-session");
    }

    @Test
    void cartControllerUsesCookieForGuestCartAndRefreshesIt() {
        CartResponse cartResponse = new CartResponse();
        when(securityUtils.isAuthenticated()).thenReturn(false);
        when(servletRequest.getCookies()).thenReturn(new Cookie[]{new Cookie("CART_SESSION_ID", "cookie-session")});
        when(cartService.getCartBySession("cookie-session")).thenReturn(cartResponse);

        assertThat(cartController.getUserCart().getBody()).isSameAs(cartResponse);

        verify(cartService).getCartBySession("cookie-session");
        verify(servletResponse).addCookie(any(Cookie.class));
    }

    @Test
    void cartControllerUsesHeaderForGuestSessionWhenCookieIsMissing() {
        CartItemRequest request = new CartItemRequest();
        CartResponse cartResponse = new CartResponse();

        when(securityUtils.isAuthenticated()).thenReturn(false);
        when(servletRequest.getCookies()).thenReturn(null);
        when(servletRequest.getHeader("X-Session-ID")).thenReturn("header-session");
        when(cartService.addItemToSessionCart("header-session", request)).thenReturn(cartResponse);

        assertThat(cartController.addItemToCart(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(cartController.addItemToCart(request).getBody()).isSameAs(cartResponse);
        verify(cartService, times(2)).addItemToSessionCart("header-session", request);
        verify(servletResponse, times(2)).addCookie(any(Cookie.class));
    }

    @Test
    void cartControllerGeneratesSessionIdWhenNoCookieOrHeaderExists() {
        CartResponse cartResponse = new CartResponse();

        when(securityUtils.isAuthenticated()).thenReturn(false);
        when(servletRequest.getCookies()).thenReturn(null);
        when(servletRequest.getHeader("X-Session-ID")).thenReturn(null);
        when(cartService.removeItemFromSessionCart(any(), eq(6L))).thenReturn(cartResponse);
        when(cartService.getCartItemCount(any(), eq(false))).thenReturn(3);

        var removeResponse = cartController.removeItemFromCart(6L);
        var countResponse = cartController.getCartItemCount();

        assertThat(removeResponse.getBody()).isSameAs(cartResponse);
        assertThat(countResponse.getBody()).isEqualTo(3);

        ArgumentCaptor<String> sessionCaptor = ArgumentCaptor.forClass(String.class);
        verify(cartService).removeItemFromSessionCart(sessionCaptor.capture(), eq(6L));
        assertThat(sessionCaptor.getValue()).isNotBlank();
        verify(servletResponse, times(2)).addCookie(any(Cookie.class));
        verify(servletResponse, times(2)).setHeader(eq("X-Session-ID"), any());
    }
}