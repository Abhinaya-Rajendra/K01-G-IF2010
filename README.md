# 🍳 Nimonscooked

![Nimonscooked Banner](assets/banner_placeholder.png)

> **Tugas Besar Pemrograman Berorientasi Objek (OOP)**
>
> *A chaotic, cute, and fast-paced cooking simulation game built entirely with Java Swing.*

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/Java_Swing-GUI-blue?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

## 📖 About The Game

**Nimonscooked** adalah game simulasi memasak yang terinspirasi oleh *Overcooked!*. Pemain mengontrol karakter chef yang lucu (Nimon) untuk menyiapkan, memasak, dan menyajikan pesanan sebelum waktu habis.

Game ini dikembangkan dari nol menggunakan **Java** tanpa *game engine* eksternal, melainkan memanfaatkan library **Swing** dan **AWT** untuk rendering grafis, serta menerapkan prinsip-prinsip **OOP** yang kuat.

---

## ✨ Key Features

* **🦊 Cute Characters:** Bermain sebagai Chef Rubah atau Rakun dalam gaya visual *3D Rendered Orthographic* yang menggemaskan.
* **🔪 Dynamic Cooking System:**
    * **Chopping:** Potong bahan mentah seperti tomat, daging, dan ikan.
    * **Cooking:** Rebus pasta di panci atau goreng daging di wajan. Hati-hati jangan sampai gosong! (🔥)
    * **Plating:** Tata makanan di piring sesuai resep.
* **📋 Order Management:** Sistem pesanan dinamis dengan batas waktu. Semakin cepat kamu menyajikan, semakin tinggi skornya.
* **🧼 Dish Washing:** Piring kotor harus dicuci sebelum bisa digunakan kembali.
* **🗺️ Multiple Stages:** Tantangan berbeda di setiap level dengan tata letak dapur yang unik.
* **🧱 Robust Physics:** Sistem *collision detection* berbasis Tile agar pergerakan karakter mulus dan tidak menembus tembok.

---

## 📸 Screenshots

| Main Menu | Gameplay |
|:---:|:---:|
| ![Menu](assets/menu_preview.jpg) | ![Gameplay](assets/gameplay_preview.jpg) |
| *Start your journey* | *Chaos in the kitchen!* |

---

## 🎮 Controls

| Key | Action |
| :---: | :--- |
| **W / A / S / D** | Bergerak (Atas, Kiri, Bawah, Kanan) |
| **SPACE** | Interaksi (Ambil/Taruh Bahan, Potong, Cuci) |
| **F** | Lempar (Throw) |
| **SHIFT** | Lari (Dash) |
| **ESC** | Pause |

---

## 👨‍🍳 How to Play

1.  **Lihat Pesanan:** Perhatikan pojok kanan atas untuk melihat resep yang diminta pelanggan (misal: *Pasta Bolognese*).
2.  **Ambil Bahan:** Ambil bahan mentah dari peti (Crate).
3.  **Proses Bahan:**
    * Tomat/Daging/Ikan/Udang -> Potong di **Cutting Board**.
    * Pasta -> Rebus di **Pot**.
    * Daging/Ikan -> Goreng di **Pan**.
4.  **Perhatikan Status:**
    * Jangan biarkan masakan terlalu lama di kompor atau akan menjadi **Gosong (Burned)** dan harus dibuang ke sampah!
5.  **Sajikan (Plating):** Taruh bahan yang sudah matang di atas **Piring**.
6.  **Serve:** Bawa piring berisi makanan jadi ke **Serving Window**.
7.  **Cuci Piring:** Piring kotor akan kembali. Cuci di wastafel agar bisa dipakai lagi.

---

## 🛠️ Technical Overview (OOP Concepts)

Game ini dibangun dengan arsitektur **MVC (Model-View-Controller)** dan menerapkan konsep OOP:

* **Encapsulation:** Mengamankan state game (seperti `cookingProgress`, `score`, `timeLeft`) dengan akses modifier private dan getter/setter.
* **Inheritance:**
    * `Item` adalah parent class dari `Ingredient`, `KitchenUtensil`, dan `Plate`.
    * `Station` adalah parent class untuk `CuttingStation`, `CookingStation`, dll.
* **Polymorphism:**
    * Method `interact()` yang di-override di setiap stasiun (meja potong perilakunya beda dengan kompor).
    * Rendering aset yang dinamis berdasarkan tipe objek.
* **Observer Pattern:** Digunakan untuk komunikasi antara *Game Logic* (Model) dan *UI* (View) tanpa ketergantungan langsung.
* **Multithreading:** Setiap proses memasak (Cooking/Burning) berjalan pada Thread terpisah agar tidak membekukan *Main Game Loop*.

---

## 🚀 Installation & Run

Pastikan kamu sudah menginstal **Java Development Kit (JDK) 8** atau lebih baru.

1.  **Clone Repository**
    ```bash
    git clone [https://github.com/username/nimonscooked.git](https://github.com/username/nimonscooked.git)
    cd nimonscooked
    ```

2.  **Compile Code**
    ```bash
    javac -d bin src/com/nimonscooked/Main.java
    ```

3.  **Run Game**
    ```bash
    java -cp bin com.nimonscooked.Main
    ```

*Atau cukup buka project ini menggunakan **IntelliJ IDEA / Eclipse / NetBeans** dan tekan Run.*

---

Made with ❤️ and a lot of ☕ code.

