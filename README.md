# ScratchCard

An Android Scratch Card View built in Java. This module allows users to scratch off a foreground layer (color or image) to reveal hidden content underneath. Perfect for implementing rewards, discounts, games, and promotional UIs.

✨ Features
Realistic scratch effect using touch gestures

Customizable overlay (color/image)

Adjustable scratch brush size

Scratch percentage tracking

Callback for scratch completion

Lightweight, no external dependencies

🧩 Integration
Step 1: Add the View to your layout
xml
Copy
Edit
<com.yourpackage.ScratchCardView
    android:id="@+id/scratchCard"
    android:layout_width="300dp"
    android:layout_height="200dp"
    app:overlayColor="#CCCCCC"
    app:brushSize="40"/>
Step 2: Set hidden content and listener in Java
java
ScratchCardView scratchCard = findViewById(R.id.scratchCard);

scratchCard.setRevealListener(() -> {
    // Action when scratch is complete
    Toast.makeText(this, "You Won!", Toast.LENGTH_SHORT).show();
});

// Optional: Track scratch percentage
scratchCard.setOnScratchProgressChangedListener(percent -> {
    Log.d("ScratchProgress", "Scratched: " + percent + "%");
});
📦 Requirements
Android API 21+

Written entirely in Java

No third-party dependencies

📄 License
MIT License — use freely in commercial and personal projects.
Scratch Card
