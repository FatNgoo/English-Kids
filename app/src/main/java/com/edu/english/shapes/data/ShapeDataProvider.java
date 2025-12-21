package com.edu.english.shapes.data;

import com.edu.english.R;
import com.edu.english.shapes.models.Shape;
import com.edu.english.shapes.models.ShapeObject;
import com.edu.english.shapes.models.ShapeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Data provider for all shapes and their associated objects
 */
public class ShapeDataProvider {

    private static ShapeDataProvider instance;
    private List<Shape> shapes;

    private ShapeDataProvider() {
        initializeShapes();
    }

    public static ShapeDataProvider getInstance() {
        if (instance == null) {
            instance = new ShapeDataProvider();
        }
        return instance;
    }

    private void initializeShapes() {
        shapes = new ArrayList<>();

        // SQUARE objects
        Shape square = new Shape(ShapeType.SQUARE);
        square.addObject(new ShapeObject("Window", "Cửa sổ", R.drawable.obj_window, "obj_window", ShapeType.SQUARE));
        square.addObject(new ShapeObject("Bread", "Bánh mì", R.drawable.obj_bread, "obj_bread", ShapeType.SQUARE));
        square.addObject(new ShapeObject("Dice", "Xúc xắc", R.drawable.obj_dice, "obj_dice", ShapeType.SQUARE));
        square.addObject(new ShapeObject("Cake", "Bánh", R.drawable.obj_cake, "obj_cake", ShapeType.SQUARE));
        square.addObject(new ShapeObject("Cushion", "Gối", R.drawable.obj_cushion, "obj_cushion", ShapeType.SQUARE));
        square.addObject(new ShapeObject("Gift Box", "Hộp quà", R.drawable.obj_gift_box, "obj_gift_box", ShapeType.SQUARE));
        shapes.add(square);

        // CIRCLE objects
        Shape circle = new Shape(ShapeType.CIRCLE);
        circle.addObject(new ShapeObject("Ball", "Quả bóng", R.drawable.obj_ball, "obj_ball", ShapeType.CIRCLE));
        circle.addObject(new ShapeObject("Sun", "Mặt trời", R.drawable.obj_sun, "obj_sun", ShapeType.CIRCLE));
        circle.addObject(new ShapeObject("Clock", "Đồng hồ", R.drawable.obj_clock, "obj_clock", ShapeType.CIRCLE));
        circle.addObject(new ShapeObject("Coin", "Đồng xu", R.drawable.obj_coin, "obj_coin", ShapeType.CIRCLE));
        circle.addObject(new ShapeObject("Plate", "Đĩa", R.drawable.obj_plate, "obj_plate", ShapeType.CIRCLE));
        circle.addObject(new ShapeObject("Wheel", "Bánh xe", R.drawable.obj_wheel, "obj_wheel", ShapeType.CIRCLE));
        shapes.add(circle);

        // TRIANGLE objects
        Shape triangle = new Shape(ShapeType.TRIANGLE);
        triangle.addObject(new ShapeObject("Pizza", "Pizza", R.drawable.obj_pizza, "obj_pizza", ShapeType.TRIANGLE));
        triangle.addObject(new ShapeObject("Mountain", "Núi", R.drawable.obj_mountain, "obj_mountain", ShapeType.TRIANGLE));
        triangle.addObject(new ShapeObject("Roof", "Mái nhà", R.drawable.obj_roof, "obj_roof", ShapeType.TRIANGLE));
        triangle.addObject(new ShapeObject("Slice", "Miếng", R.drawable.obj_slice, "obj_slice", ShapeType.TRIANGLE));
        triangle.addObject(new ShapeObject("Traffic Sign", "Biển báo", R.drawable.obj_traffic_sign, "obj_traffic_sign", ShapeType.TRIANGLE));
        triangle.addObject(new ShapeObject("Cheese", "Phô mai", R.drawable.obj_cheese, "obj_cheese", ShapeType.TRIANGLE));
        shapes.add(triangle);

        // RECTANGLE objects
        Shape rectangle = new Shape(ShapeType.RECTANGLE);
        rectangle.addObject(new ShapeObject("Door", "Cửa", R.drawable.obj_door, "obj_door", ShapeType.RECTANGLE));
        rectangle.addObject(new ShapeObject("Book", "Sách", R.drawable.obj_book, "obj_book", ShapeType.RECTANGLE));
        rectangle.addObject(new ShapeObject("Phone", "Điện thoại", R.drawable.obj_phone, "obj_phone", ShapeType.RECTANGLE));
        rectangle.addObject(new ShapeObject("TV", "Tivi", R.drawable.obj_tv, "obj_tv", ShapeType.RECTANGLE));
        rectangle.addObject(new ShapeObject("Table", "Bàn", R.drawable.obj_table, "obj_table", ShapeType.RECTANGLE));
        rectangle.addObject(new ShapeObject("Bed", "Giường", R.drawable.obj_bed, "obj_bed", ShapeType.RECTANGLE));
        shapes.add(rectangle);

        // OVAL objects
        Shape oval = new Shape(ShapeType.OVAL);
        oval.addObject(new ShapeObject("Egg", "Trứng", R.drawable.obj_egg, "obj_egg", ShapeType.OVAL));
        oval.addObject(new ShapeObject("Balloon", "Bóng bay", R.drawable.obj_balloon, "obj_balloon", ShapeType.OVAL));
        oval.addObject(new ShapeObject("Mirror", "Gương", R.drawable.obj_mirror, "obj_mirror", ShapeType.OVAL));
        oval.addObject(new ShapeObject("Leaf", "Lá", R.drawable.obj_leaf, "obj_leaf", ShapeType.OVAL));
        oval.addObject(new ShapeObject("Rugby Ball", "Bóng bầu dục", R.drawable.obj_rugby_ball, "obj_rugby_ball", ShapeType.OVAL));
        oval.addObject(new ShapeObject("Soap", "Xà phòng", R.drawable.obj_soap, "obj_soap", ShapeType.OVAL));
        shapes.add(oval);

        // STAR objects
        Shape star = new Shape(ShapeType.STAR);
        star.addObject(new ShapeObject("Star", "Ngôi sao", R.drawable.obj_star, "obj_star", ShapeType.STAR));
        star.addObject(new ShapeObject("Sticker", "Nhãn dán", R.drawable.obj_sticker, "obj_sticker", ShapeType.STAR));
        star.addObject(new ShapeObject("Medal", "Huy chương", R.drawable.obj_medal, "obj_medal", ShapeType.STAR));
        star.addObject(new ShapeObject("Cookie", "Bánh quy", R.drawable.obj_cookie, "obj_cookie", ShapeType.STAR));
        star.addObject(new ShapeObject("Badge", "Huy hiệu", R.drawable.obj_badge, "obj_badge", ShapeType.STAR));
        star.addObject(new ShapeObject("Decoration", "Trang trí", R.drawable.obj_decoration, "obj_decoration", ShapeType.STAR));
        shapes.add(star);
    }

    public List<Shape> getAllShapes() {
        return shapes;
    }

    public Shape getShape(ShapeType type) {
        for (Shape shape : shapes) {
            if (shape.getType() == type) {
                return shape;
            }
        }
        return null;
    }

    public Shape getShapeByIndex(int index) {
        if (index >= 0 && index < shapes.size()) {
            return shapes.get(index);
        }
        return null;
    }

    public int getShapeCount() {
        return shapes.size();
    }

    public void resetProgress() {
        for (Shape shape : shapes) {
            shape.setCompleted(false);
            shape.setTracingProgress(0);
        }
    }
}
