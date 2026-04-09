import os
from PIL import Image, ImageDraw

src_img = r"C:\Users\ALISH\.gemini\antigravity\brain\7c17d7b0-12cb-4160-a0a5-be58b92deeb1\autoexpense_logo_1775753622335.png"
base_dir = r"c:\Users\ALISH\Desktop\payment tracker\app\src\main\res"

sizes = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192
}

try:
    img = Image.open(src_img).convert("RGBA")

    # 1. Legacy Launcher Icons (Square & Round)
    for dpi, size in sizes.items():
        resized = img.resize((size, size), Image.Resampling.LANCZOS)
        out_dir = os.path.join(base_dir, f"mipmap-{dpi}")
        os.makedirs(out_dir, exist_ok=True)
        
        # Save standard
        resized.save(os.path.join(out_dir, "ic_launcher.png"))
        
        # Create round mask
        mask = Image.new("L", (size, size), 0)
        draw = ImageDraw.Draw(mask)
        draw.ellipse((0, 0, size, size), fill=255)
        
        round_img = Image.new("RGBA", (size, size), (0,0,0,0))
        round_img.paste(resized, mask=mask)
        round_img.save(os.path.join(out_dir, "ic_launcher_round.png"))

    # 2. Adaptive Icon Foreground (108x108, center shape should fit in 72x72)
    fg_size = 108
    safe_zone = 72
    
    # Let's crop the image to a circle before putting in foreground to make it look nice?
    # Or just resize the whole square image to fit within the safe zone
    fg_img = Image.new("RGBA", (fg_size, fg_size), (0,0,0,0))
    resized_center = img.resize((safe_zone, safe_zone), Image.Resampling.LANCZOS)
    
    # Optionally mask it as circle to make adaptive icon look uniform
    mask_safe = Image.new("L", (safe_zone, safe_zone), 0)
    draw_safe = ImageDraw.Draw(mask_safe)
    draw_safe.ellipse((0, 0, safe_zone, safe_zone), fill=255)
    
    masked_center = Image.new("RGBA", (safe_zone, safe_zone), (0,0,0,0))
    masked_center.paste(resized_center, mask=mask_safe)
    
    offset = (fg_size - safe_zone) // 2
    fg_img.paste(masked_center, (offset, offset), masked_center)
    
    draw_dir = os.path.join(base_dir, "drawable-v24")
    os.makedirs(draw_dir, exist_ok=True)
    fg_img.save(os.path.join(draw_dir, "ic_launcher_foreground.png"))
    
    print("SUCCESS: Icons generated successfully.")
except Exception as e:
    print("ERROR:", e)
