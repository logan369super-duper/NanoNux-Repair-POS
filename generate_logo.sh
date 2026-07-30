#!/bin/bash

# Create high-res 1024x1024 logo
convert -size 1024x1024 xc:white \
  -fill black -font DejaVu-Sans-Bold -pointsize 230 -draw "text 70,280 'NANO'" \
  -fill black -font DejaVu-Sans-Bold -pointsize 230 -draw "text 440,540 'NUX'" \
  -fill black -draw "roundrectangle 70,650,954,920,60,60" \
  -fill white -font DejaVu-Sans-Bold -pointsize 135 -gravity center -draw "text 0,280 'Repair POS'" \
  /tmp/logo_1024.png

# Copy to drawable
cp /tmp/logo_1024.png app/src/main/res/drawable/ic_app_logo.png
cp /tmp/logo_1024.png app/src/main/res/drawable/ic_launcher_foreground.png

# Resize for mipmap targets
convert /tmp/logo_1024.png -resize 48x48 app/src/main/res/mipmap-mdpi/ic_launcher.png
convert /tmp/logo_1024.png -resize 48x48 app/src/main/res/mipmap-mdpi/ic_launcher_round.png

convert /tmp/logo_1024.png -resize 72x72 app/src/main/res/mipmap-hdpi/ic_launcher.png
convert /tmp/logo_1024.png -resize 72x72 app/src/main/res/mipmap-hdpi/ic_launcher_round.png

convert /tmp/logo_1024.png -resize 96x96 app/src/main/res/mipmap-xhdpi/ic_launcher.png
convert /tmp/logo_1024.png -resize 96x96 app/src/main/res/mipmap-xhdpi/ic_launcher_round.png

convert /tmp/logo_1024.png -resize 144x144 app/src/main/res/mipmap-xxhdpi/ic_launcher.png
convert /tmp/logo_1024.png -resize 144x144 app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png

convert /tmp/logo_1024.png -resize 192x192 app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
convert /tmp/logo_1024.png -resize 192x192 app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png

echo "Generated all PNG logos!"
