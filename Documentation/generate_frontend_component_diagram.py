from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


WIDTH = 2400
HEIGHT = 1300
BG = "#F5F7FB"
TEXT = "#132744"
MUTED = "#617287"
LINE = "#6B7D95"
PANEL = "#FCFDFE"


OUTPUT_PATH = Path(__file__).with_name("Hobby Shop Front End Component Diagram.png")


def load_font(size: int, bold: bool = False):
    candidates = []
    if bold:
        candidates.extend(
            [
                Path("C:/Windows/Fonts/arialbd.ttf"),
                Path("C:/Windows/Fonts/seguisb.ttf"),
                Path("C:/Windows/Fonts/segoeuib.ttf"),
            ]
        )
    else:
        candidates.extend(
            [
                Path("C:/Windows/Fonts/arial.ttf"),
                Path("C:/Windows/Fonts/segoeui.ttf"),
            ]
        )

    for candidate in candidates:
        if candidate.exists():
            return ImageFont.truetype(str(candidate), size=size)

    return ImageFont.load_default()


TITLE_FONT = load_font(42, bold=True)
SUBTITLE_FONT = load_font(22)
SECTION_FONT = load_font(24, bold=True)
NODE_FONT = load_font(22, bold=True)
BODY_FONT = load_font(20)
SMALL_FONT = load_font(18)


PALETTE = {
    "root": ("#EAF3FF", "#1E63B5"),
    "provider": ("#FFF3D6", "#D28B00"),
    "route": ("#F2E9FF", "#7A4BC2"),
    "layout": ("#E7FAF7", "#167C71"),
    "page": ("#EAF7E8", "#2C7A2C"),
    "admin": ("#FFF0EA", "#C35E2C"),
    "shared": ("#EDF2FF", "#4667D7"),
    "service": ("#F1F3F6", "#66758B"),
}


def text_size(draw: ImageDraw.ImageDraw, text: str, font):
    left, top, right, bottom = draw.multiline_textbbox((0, 0), text, font=font, spacing=6, align="center")
    return right - left, bottom - top


def draw_centered_text(draw, box, text, font, fill=TEXT):
    x1, y1, x2, y2 = box
    width, height = text_size(draw, text, font)
    tx = x1 + (x2 - x1 - width) / 2
    ty = y1 + (y2 - y1 - height) / 2
    draw.multiline_text((tx, ty), text, font=font, fill=fill, spacing=6, align="center")


def draw_panel(draw, box, title):
    draw.rounded_rectangle(box, radius=28, fill=PANEL, outline="#D5DEEA", width=2)
    label_box = (box[0] + 18, box[1] + 16, box[0] + 320, box[1] + 62)
    draw.rounded_rectangle(label_box, radius=16, fill="#FFFFFF", outline="#D5DEEA", width=2)
    draw_centered_text(draw, label_box, title, SECTION_FONT)


def draw_node(draw, center_x, top_y, width, height, text, kind, font=NODE_FONT):
    fill, outline = PALETTE[kind]
    box = (center_x - width // 2, top_y, center_x + width // 2, top_y + height)
    shadow = (box[0] + 4, box[1] + 4, box[2] + 4, box[3] + 4)
    draw.rounded_rectangle(shadow, radius=18, fill="#DEE5EF")
    draw.rounded_rectangle(box, radius=18, fill=fill, outline=outline, width=4)
    draw_centered_text(draw, box, text, font)
    return box


def top_mid(box):
    return ((box[0] + box[2]) / 2, box[1])


def bottom_mid(box):
    return ((box[0] + box[2]) / 2, box[3])


def left_mid(box):
    return (box[0], (box[1] + box[3]) / 2)


def draw_arrow(draw, start, end, color=LINE, width=4):
    sx, sy = start
    ex, ey = end
    draw.line((sx, sy, ex, ey), fill=color, width=width)
    size = 12
    points = [(ex, ey), (ex - 6, ey - size), (ex + 6, ey - size)]
    draw.polygon(points, fill=color)


def draw_right_arrow(draw, start, end, color=LINE, width=4):
    sx, sy = start
    ex, ey = end
    draw.line((sx, sy, ex, ey), fill=color, width=width)
    size = 12
    points = [(ex, ey), (ex - size, ey - 6), (ex - size, ey + 6)]
    draw.polygon(points, fill=color)


def draw_polyline_arrow(draw, points, color=LINE, width=4):
    for start, end in zip(points, points[1:-1]):
        draw.line((*start, *end), fill=color, width=width)
    draw_right_arrow(draw, points[-2], points[-1], color=color, width=width)


def main():
    image = Image.new("RGB", (WIDTH, HEIGHT), BG)
    draw = ImageDraw.Draw(image)

    draw.text((WIDTH / 2 - 430, 28), "Hobby Shop Frontend Component Diagram", fill=TEXT, font=TITLE_FONT)
    draw.text(
        (WIDTH / 2 - 500, 84),
        "Left-to-right architecture view for presentation: how the React app is composed from shell to routes to shared UI and data",
        fill=MUTED,
        font=SUBTITLE_FONT,
    )

    draw_panel(draw, (70, 135, 2330, 1210), "Left-to-Right View")

    col1 = 260
    col2 = 760
    col3 = 1280
    col4 = 1810

    app_shell = draw_node(
        draw,
        col1,
        250,
        360,
        190,
        "App Shell\nApp.jsx\nErrorBoundary\nBrowserRouter\nScrollToTop",
        "root",
    )

    providers = draw_node(
        draw,
        col1,
        560,
        360,
        210,
        "State Providers\nAuthProvider\nCartProvider\nAuthContext + useAuth\nCartContext + useCart",
        "provider",
    )

    route_hub = draw_node(
        draw,
        col2,
        320,
        360,
        120,
        "Routing Hub\nAppRoutes",
        "route",
    )

    guards = draw_node(
        draw,
        col2,
        560,
        360,
        170,
        "Route Guards\nPublic routes\nProtectedRoute\nAdminRoute",
        "route",
    )

    public_pages = draw_node(
        draw,
        col3,
        190,
        420,
        250,
        "Public Pages\nHomePage\nProductsPage • ProductDetailPage\nCategoryPage • BrandPage\nCartPage\nLoginPage • RegisterPage\nNotFoundPage",
        "page",
        font=BODY_FONT,
    )

    protected_pages = draw_node(
        draw,
        col3,
        515,
        420,
        210,
        "Protected Pages\nCheckoutPage\nOrdersPage • OrderDetailPage\nOrderSuccessPage\nProfilePage",
        "layout",
        font=BODY_FONT,
    )

    admin_pages = draw_node(
        draw,
        col3,
        805,
        420,
        250,
        "Admin Pages\nDashboard\nProducts • Brands • Categories\nOrders • Customers\nCreate/Edit/Detail screens",
        "admin",
        font=BODY_FONT,
    )

    layout = draw_node(
        draw,
        col4,
        255,
        430,
        170,
        "Shared Layout\nMainLayout\nNavbar\npage content\nFooter",
        "layout",
    )

    shared_ui = draw_node(
        draw,
        col4,
        515,
        430,
        220,
        "Shared UI Components\nPillNav\nProductGrid -> ProductCard\nCartItemRow\nHomePage pieces:\nHeroBanner, FeaturedProducts",
        "shared",
        font=BODY_FONT,
    )

    data_layer = draw_node(
        draw,
        col4,
        835,
        430,
        180,
        "Data Layer\nauthService.js\ncartService.js\napi.js",
        "service",
    )

    draw_right_arrow(draw, (col1 + 180, 345), (col2 - 180, 380))
    draw_right_arrow(draw, (col1 + 180, 665), (col2 - 180, 645))

    draw_right_arrow(draw, (col2 + 180, 380), (col3 - 210, 315))
    draw_right_arrow(draw, (col2 + 180, 645), (col3 - 210, 620))
    draw_right_arrow(draw, (col2 + 180, 645), (col3 - 210, 930))

    draw_right_arrow(draw, (col3 + 210, 315), (col4 - 215, 340))
    draw_right_arrow(draw, (col3 + 210, 620), (col4 - 215, 340))
    draw_right_arrow(draw, (col3 + 210, 930), (col4 - 215, 340))

    draw_right_arrow(draw, (col3 + 210, 315), (col4 - 215, 625))
    draw_polyline_arrow(
        draw,
        [
            (col1 + 180, 665),
            (720, 1070),
            (1510, 1070),
            (1510, 925),
            left_mid(data_layer),
        ],
    )

    notes_box = (140, 1090, 2260, 1175)
    draw.rounded_rectangle(notes_box, radius=18, fill="#FFFFFF", outline="#D5DEEA", width=2)
    note = (
        "How to present it: read from left to right. App Shell and Providers establish the application, "
        "AppRoutes and route guards decide access, page groups represent user-facing screens, and the far-right column shows shared layout, reusable UI, and API access."
    )
    draw_centered_text(draw, notes_box, note, SMALL_FONT, fill=MUTED)

    image.save(OUTPUT_PATH, "PNG")
    print(f"Updated diagram written to: {OUTPUT_PATH}")


if __name__ == "__main__":
    main()