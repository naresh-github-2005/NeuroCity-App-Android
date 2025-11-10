# NeuroCity UI Components - Quick Reference Guide

## 🎨 Using Enhanced UI Components

### Colors

#### Primary Colors
```xml
<!-- In layouts -->
android:background="@color/primary_color"
android:textColor="@color/primary_color"

<!-- Available colors -->
@color/primary_color          <!-- #667EEA -->
@color/primary_dark           <!-- #5A67D8 -->
@color/primary_light          <!-- #E0E7FF -->
```

#### Status Colors
```xml
@color/statusPending          <!-- #F59E0B - Amber -->
@color/statusInProgress       <!-- #3B82F6 - Blue -->
@color/statusResolved         <!-- #10B981 - Green -->
@color/statusRejected         <!-- #EF4444 - Red -->
```

#### Text Colors
```xml
@color/textColorPrimary       <!-- Main text -->
@color/textColorSecondary     <!-- Secondary text -->
@color/text_tertiary_light    <!-- Tertiary text -->
```

### Buttons

#### Primary Gradient Button
```xml
<com.google.android.material.button.MaterialButton
    android:layout_width="match_parent"
    android:layout_height="@dimen/button_height_lg"
    android:text="Button Text"
    android:textColor="@android:color/white"
    android:textSize="@dimen/text_md"
    android:textStyle="bold"
    android:background="@drawable/button_gradient_primary"
    app:cornerRadius="@dimen/radius_md"
    android:stateListAnimator="@null" />
```

#### Secondary Outline Button
```xml
<com.google.android.material.button.MaterialButton
    android:layout_width="match_parent"
    android:layout_height="@dimen/button_height_lg"
    android:text="Button Text"
    android:textColor="@color/primary_color"
    android:textSize="@dimen/text_md"
    android:textStyle="bold"
    android:backgroundTint="@color/secondary_button_bg"
    app:cornerRadius="@dimen/radius_md"
    app:strokeColor="@color/primary_color"
    app:strokeWidth="2dp"
    android:stateListAnimator="@null" />
```

#### Success Gradient Button
```xml
<com.google.android.material.button.MaterialButton
    android:layout_width="match_parent"
    android:layout_height="@dimen/button_height_md"
    android:text="Submit"
    android:background="@drawable/button_gradient_success"
    android:textColor="@android:color/white"
    app:cornerRadius="@dimen/radius_md"
    android:stateListAnimator="@null" />
```

#### Rounded Filter Button
```xml
<com.google.android.material.button.MaterialButton
    android:layout_width="wrap_content"
    android:layout_height="@dimen/button_height_md"
    android:text="Filter"
    app:cornerRadius="@dimen/radius_full"
    app:backgroundTint="@color/statusPending"
    android:textColor="@android:color/white"
    android:paddingStart="@dimen/spacing_lg"
    android:paddingEnd="@dimen/spacing_lg"
    android:stateListAnimator="@null" />
```

### Cards

#### Standard Card
```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="@dimen/card_margin"
    app:cardCornerRadius="@dimen/radius_lg"
    app:cardElevation="@dimen/elevation_sm"
    app:cardBackgroundColor="@color/cardBackgroundColor">
    
    <!-- Card content -->
    
</com.google.android.material.card.MaterialCardView>
```

#### Image Card with Frame
```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cardCornerRadius="@dimen/radius_md"
    app:cardElevation="0dp">
    
    <ImageView
        android:layout_width="match_parent"
        android:layout_height="240dp"
        android:scaleType="centerCrop" />
        
</com.google.android.material.card.MaterialCardView>
```

### Status Badges

```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Pending"
    android:textSize="@dimen/text_xs"
    android:textStyle="bold"
    android:paddingStart="@dimen/spacing_md"
    android:paddingEnd="@dimen/spacing_md"
    android:paddingTop="@dimen/spacing_sm"
    android:paddingBottom="@dimen/spacing_sm"
    android:background="@drawable/status_badge_pending"
    android:textColor="@android:color/white" />
```

### Spacing

Use consistent spacing throughout:
```xml
android:padding="@dimen/spacing_md"        <!-- 16dp -->
android:margin="@dimen/spacing_lg"         <!-- 24dp -->
android:layout_marginTop="@dimen/spacing_sm"  <!-- 8dp -->
```

Available spacing:
- `spacing_xs` - 4dp
- `spacing_sm` - 8dp
- `spacing_md` - 16dp (default)
- `spacing_lg` - 24dp
- `spacing_xl` - 32dp
- `spacing_xxl` - 48dp

### Dimensions

#### Button Heights
```xml
@dimen/button_height_sm    <!-- 40dp -->
@dimen/button_height_md    <!-- 48dp -->
@dimen/button_height_lg    <!-- 56dp -->
```

#### Corner Radius
```xml
@dimen/radius_sm           <!-- 8dp -->
@dimen/radius_md           <!-- 12dp - default for buttons -->
@dimen/radius_lg           <!-- 16dp - default for cards -->
@dimen/radius_xl           <!-- 20dp -->
@dimen/radius_full         <!-- 999dp - pill shape -->
```

#### Elevation
```xml
@dimen/elevation_xs        <!-- 2dp -->
@dimen/elevation_sm        <!-- 4dp - default for cards -->
@dimen/elevation_md        <!-- 8dp - bottom nav -->
@dimen/elevation_lg        <!-- 12dp -->
@dimen/elevation_xl        <!-- 16dp -->
```

#### Icon Sizes
```xml
@dimen/icon_size_sm        <!-- 20dp -->
@dimen/icon_size_md        <!-- 24dp - default -->
@dimen/icon_size_lg        <!-- 32dp -->
@dimen/icon_size_xl        <!-- 48dp -->
```

#### Text Sizes
```xml
@dimen/text_xs             <!-- 12sp - captions -->
@dimen/text_sm             <!-- 14sp - body small -->
@dimen/text_md             <!-- 16sp - body -->
@dimen/text_lg             <!-- 18sp - subtitle -->
@dimen/text_xl             <!-- 20sp -->
@dimen/text_xxl            <!-- 24sp -->
@dimen/text_title          <!-- 28sp -->
@dimen/text_headline       <!-- 32sp -->
```

### Typography

#### Headlines
```xml
<TextView
    android:text="Headline"
    android:textSize="@dimen/text_headline"
    android:textStyle="bold"
    android:textColor="@color/textColorPrimary"
    android:fontFamily="sans-serif-medium"
    android:letterSpacing="0.02" />
```

#### Body Text
```xml
<TextView
    android:text="Body text"
    android:textSize="@dimen/text_md"
    android:textColor="@color/textColorPrimary"
    android:lineSpacingExtra="4dp" />
```

#### Caption/Secondary
```xml
<TextView
    android:text="Caption"
    android:textSize="@dimen/text_xs"
    android:textColor="@color/textColorSecondary" />
```

### Input Fields

```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.App.TextInputLayout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Email"
    app:startIconDrawable="@android:drawable/ic_dialog_email">
    
    <com.google.android.material.textfield.TextInputEditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:inputType="textEmailAddress" />
        
</com.google.android.material.textfield.TextInputLayout>
```

### FAB (Floating Action Button)

```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:src="@drawable/ic_add"
    android:layout_gravity="bottom|end"
    android:layout_margin="@dimen/spacing_md"
    app:backgroundTint="@color/primary_color"
    app:tint="@android:color/white"
    app:elevation="@dimen/elevation_sm"
    app:borderWidth="0dp" />
```

### Dividers

```xml
<View
    android:layout_width="match_parent"
    android:layout_height="1dp"
    android:background="@color/dividerColor"
    android:layout_marginTop="@dimen/spacing_md"
    android:layout_marginBottom="@dimen/spacing_md" />
```

## 🌓 Dark Mode Support

All colors automatically switch in dark mode. No additional code needed!

## 💡 Tips

1. **Always use dimension resources** instead of hardcoded values
2. **Use semantic color names** (textColorPrimary, not #000000)
3. **Apply consistent spacing** using the spacing scale
4. **Remove default elevation animations** with `android:stateListAnimator="@null"` for custom backgrounds
5. **Use proper icon sizes** based on context (20dp for small, 24dp for normal)
6. **Apply letter spacing** to buttons and headlines for modern look
7. **Use fontFamily="sans-serif-medium"** for headings
8. **Add lineSpacingExtra** to body text for readability

## 📚 Custom Styles Available

- `Widget.App.Button` - Enhanced button style
- `Widget.App.CardView` - Enhanced card style
- `Widget.App.TextInputLayout` - Enhanced input style
- `Widget.App.Toolbar` - Enhanced toolbar style
- `Widget.App.FloatingActionButton` - Enhanced FAB style
- `Widget.App.BottomNavigationView` - Enhanced bottom nav style

Use them with:
```xml
style="@style/Widget.App.Button"
```

