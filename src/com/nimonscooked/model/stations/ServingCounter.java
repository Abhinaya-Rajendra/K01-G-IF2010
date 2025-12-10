package com.nimonscooked.model.stations;

import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.Plate;
import com.nimonscooked.model.logic.GameModel;

public class ServingCounter extends Station {

    public ServingCounter(int x, int y) {
        super(x, y);
    }

    @Override
    public void interact(Chef chef) {
        Object heldItem = chef.getInventory().getItem();
        
        if (heldItem instanceof Plate) {
            Plate plate = (Plate) heldItem;
            
            if (!plate.isEmpty()) {
                GameModel gm = GameModel.getInstance();
                
                boolean isSuccess = gm.getOrderManager().validateService(plate);
                
                if (isSuccess) {
                    System.out.println("Serving Success! Score Added.");
                } else {
                    System.out.println("Serving Failed! Wrong Order.");
                    
                    // --- GAME OVER LOGIC: PENALTI ---
                    gm.addScore(-10);
                    gm.addFailedOrder(); // Trigger Failure (Salah Serving)
                    // --------------------------------
                }
                
                // Clean Up (Piring hilang baik sukses maupun gagal)
                plate.clearContents(); 
                chef.getInventory().takeItem(); 
                gm.returnPlateLater(); 
                
            } else {
                System.out.println("Cannot serve empty plate!");
            }
        } else {
            System.out.println("Must serve using a Plate!");
        }
    }
}