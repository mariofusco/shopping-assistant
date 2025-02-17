package engineering.epic.tools;

import dev.langchain4j.agent.tool.Tool;
import engineering.epic.databases.ShoppingDatabase;
import engineering.epic.endpoints.WebsocketConnectionManager;
import engineering.epic.models.Product;
import engineering.epic.state.CustomShoppingState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderTools {

    @Inject
    ShoppingDatabase shoppingDatabase;

    @Inject
    WebsocketConnectionManager connectionManager;

    CustomShoppingState customShoppingState;

    public OrderTools(CustomShoppingState customShoppingState) {
        this.customShoppingState = customShoppingState;
    }

    @Tool("will display the shopping cart to the user, takes a comma-separated list of product names")
    // TODO would ideally propose quantity per product too but we can probably get away without
    public void displayShoppingCart(String productNames) {
        System.out.println("Calling displayShoppingCart() with productNames: " + productNames);
        // TODO one day, handle string literals :p
        customShoppingState.getShoppingState().moveToStep("3. Shopping cart");

        // TODO make sure the LLM passes the names (share memory somehow)
        productNames = "Diapers,Chocolate Bar";
        List<String> productList = Arrays.asList(productNames.split(","));
        List<Map<String, Object>> productDetails = productList.stream()
                .map(name -> {
                    Product product = shoppingDatabase.getProductByName(name.trim());
                    if (product != null) {
                        Map<String, Object> details = new HashMap<>();
                        details.put("name", product.getName());
                        details.put("description", product.getDescription());
                        details.put("price", product.getPrice());
                        details.put("quantity", "1");
                        return details;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Send WebSocket message
        connectionManager.broadcastMessage("displayShoppingCart", productDetails);
    }
}
