package lk.sliit.se2030.cloth_sphere;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InventoryController {

    @GetMapping("/inventory")
    public String showInventoryPage() {
        return "inventory";  // looks for templates/inventory.html
    }
}
