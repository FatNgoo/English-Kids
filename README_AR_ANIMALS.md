# Animals AR Feature - Hướng dẫn

## 📋 Tổng quan
Tính năng Animals AR cho phép trẻ em xem các con vật trong môi trường thực tế ảo tăng cường (AR) hoặc chế độ 2D nếu thiết bị không hỗ trợ AR.

## 🚀 Cách sử dụng
1. Từ màn hình chính, bấm vào card **Animals**
2. Chọn một con vật từ danh sách 6 con
3. Thiết bị sẽ tự động mở chế độ AR hoặc 2D tùy thuộc vào khả năng hỗ trợ
4. Trong AR: Chạm vào mặt phẳng để đặt con vật
5. TTS tự động đọc tên con vật mỗi 5 giây

## 📁 Cấu trúc thư mục

### Models 3D (GLB) - CẦN THÊM THỦ CÔNG
```
app/src/main/assets/models/
├── cat.glb      ← Thay thế file .glb.txt
├── dog.glb
├── cow.glb
├── lion.glb
├── monkey.glb
└── elephant.glb
```

### Âm thanh con vật (MP3) - CẦN THÊM THỦ CÔNG
```
app/src/main/res/raw/
├── cat.mp3
├── dog.mp3
├── cow.mp3
├── lion.mp3
├── monkey.mp3
└── elephant.mp3
```

**Sau khi thêm MP3**, mở `AnimalArRepository.java` và đổi `0` thành `R.raw.cat`, `R.raw.dog`, v.v.

## 📥 Cách thêm assets thực

### 1. Thêm model 3D (.glb)
1. Tải model từ các nguồn sau:
   - [Sketchfab](https://sketchfab.com/) - Nhiều model động vật miễn phí
   - [Google Poly Archive](https://poly.pizza/) - Model đơn giản
   - [Kenney Assets](https://kenney.nl/assets) - Game assets miễn phí
   
2. Tải file `.glb` (khuyến nghị) hoặc `.gltf`

3. Đặt file vào `app/src/main/assets/models/` với đúng tên:
   - `cat.glb`, `dog.glb`, `cow.glb`, `lion.glb`, `monkey.glb`, `elephant.glb`

4. **Xóa file placeholder**: Xóa các file `.glb.txt`

### 2. Thêm âm thanh (.mp3)
1. Tìm âm thanh động vật từ:
   - [FreeSound](https://freesound.org/)
   - [ZapSplat](https://www.zapsplat.com/)
   
2. Chuyển đổi sang MP3 nếu cần

3. Đặt file vào `app/src/main/res/raw/` với đúng tên:
   - `cat.mp3`, `dog.mp3`, `cow.mp3`, `lion.mp3`, `monkey.mp3`, `elephant.mp3`

4. **Xóa file placeholder**: Xóa các file `.xml` trong thư mục `raw/`

### 3. Thêm hình ảnh thumbnail
Hiện tại đang dùng vector drawable placeholder. Để thêm ảnh thực:

1. Chuẩn bị ảnh PNG kích thước 512x512 hoặc lớn hơn
2. Đặt vào `app/src/main/res/drawable/` với đúng tên:
   - `animal_cat.png`, `animal_dog.png`, v.v.
3. **Xóa file vector**: Xóa các file `.xml` tương ứng

## ⚙️ Cấu hình

### Điều chỉnh scale model AR
Mở file `AnimalArRepository.java` và sửa giá trị `modelScale`:
```java
new AnimalArItem(
    "cat",
    "Cat", 
    "Mèo",
    "models/cat.glb",
    R.raw.cat,
    R.drawable.animal_cat,
    0.4f  // <-- Thay đổi giá trị này (0.1 - 1.0)
)
```

### Điều chỉnh thời gian TTS
Mở file `AnimalArViewerActivity.java` và sửa:
```java
private static final long TTS_INTERVAL_MS = 5000; // Milliseconds
```

## 🔧 Xử lý sự cố

### AR không hoạt động
- Đảm bảo thiết bị hỗ trợ ARCore
- Kiểm tra đã cấp quyền Camera
- App sẽ tự động fallback sang chế độ 2D nếu AR không khả dụng

### Âm thanh không phát
- Kiểm tra file MP3 có đúng định dạng
- Đảm bảo tên file đúng (lowercase, không dấu)
- Kiểm tra file placeholder đã được xóa

### Model không hiển thị
- Kiểm tra file GLB có hợp lệ
- Đảm bảo file được đặt đúng thư mục `assets/models/`
- Thử với model đơn giản hơn (ít polygons)

## 📱 Yêu cầu
- Android 7.0 (API 24) trở lên
- Đối với AR: Thiết bị hỗ trợ ARCore
- Đối với 2D: Bất kỳ thiết bị Android nào

## 📚 Thư viện sử dụng
- **SceneView** (io.github.sceneview:arsceneview) - Render AR
- **ARCore** (com.google.ar:core) - Google AR framework
- **Android TTS** - Text-to-Speech API có sẵn trong Android

## 🎨 UI/UX cho trẻ em
- Nút to, dễ bấm (min 48dp)
- Màu sắc tươi sáng
- Emoji dễ nhận biết
- Hướng dẫn rõ ràng bằng tiếng Việt
- Animation phản hồi khi bấm

## 📝 Files được tạo/sửa

### Files mới:
- `AnimalArSelectActivity.java` - Màn hình chọn con vật
- `AnimalArViewerActivity.java` - Màn hình xem AR/2D
- `AnimalArItem.java` - Model dữ liệu
- `AnimalArRepository.java` - Repository mock data
- `AnimalArAdapter.java` - RecyclerView adapter
- `ArCoreHelper.java` - Helper kiểm tra AR
- `activity_animal_ar_select.xml` - Layout chọn con vật
- `activity_animal_ar_viewer.xml` - Layout viewer
- `item_animal_ar.xml` - Layout item card
- `bg_ar_button.xml` - Background cho nút
- `animal_*.xml` - Placeholder images

### Files đã sửa:
- `MainActivity.java` - Thêm navigation
- `AndroidManifest.xml` - Thêm activities + permissions
- `build.gradle.kts` - Thêm dependencies
- `libs.versions.toml` - Thêm version catalog
