package engineering.epic.endpoints;

import engineering.epic.aiservices.HelpfulShoppingAssistant;
import engineering.epic.state.CustomShoppingState;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/helpful-assistant")
public class HelpfulAssistantResource {

    private static final Logger logger = Logger.getLogger(HelpfulAssistantResource.class);

    @Inject
    HelpfulShoppingAssistant aiShoppingAssistant;

    @Inject
    WebsocketConnectionManager connectionManager;

    @Inject
    CustomShoppingState customShoppingState;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleMessage(MessageRequest request) {
        try {
            String message = request.getMessage();
            System.out.println("Received message: " + message);

            // TODO move this to a separate decision logic place?
            // TODO reduce the tools for agent one
            // we're still deciding on the products to buy
            if (customShoppingState.getShoppingState().currentStep.startsWith("1")) {
                // TODO ideally find a way to flush the memory on page reload (so no restart required)
                String answer = aiShoppingAssistant.answer(1, message);
                // if no products proposed yet, continue conversation
                if (customShoppingState.getShoppingState().currentStep.startsWith("1")) {
                    System.out.println("AI response: " + answer);
                    MessageResponse response = new MessageResponse(answer);
                    return Response.ok(response).build();
                }
                // else, products have been proposed
                answer = "I proposed a shopping cart for you, do you want to add anything else?";
                MessageResponse response = new MessageResponse(answer);
                System.out.println("AI response: " + answer);
                return Response.ok(response).build();
            }

            // we have a proposed list
            // TODO create agent two for ordering
            if (customShoppingState.getShoppingState().currentStep.startsWith("2")) {
                System.out.println("Dealing with step 2");
                // TODO AiService to decide if they are ready to order
                // if no: customer wants to add or remove something
                // TODO propose sth else (back to agent 1)
                // if yes: customer is happy with selection: we'll order
                // TODO input: possible to take from frontend? would be ideal with next Agent in mind. or track backend?
                // TODO order this input (create order backend, frontend: move over basket with a small pause to order successfull
                // TODO and ask do you want to shop more? (STEP1 again)
            }
            MessageResponse response = new MessageResponse("Still need to build");
            System.out.println("Still need to build");
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.error("Error processing message", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new MessageResponse("An error occurred while processing your request."))
                    .build();
        }
    }

    public static class MessageRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class MessageResponse {
        private String response;

        public MessageResponse(String response) {
            this.response = response;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }
}