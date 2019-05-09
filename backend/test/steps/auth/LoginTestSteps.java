package steps.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import org.junit.Assert;
import play.Application;
import play.ApplicationLoader;
import play.Environment;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import utils.PlayResultToJson;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static play.test.Helpers.route;

public class LoginTestSteps {
    @Inject
    private Application application;

    private JsonNode userData;
    private Result result;
    private Result signUpResponse;
    private Result loginResponse;

    /**
     * Set up the backend server
     */
    @Before("@LoginSteps")
    public void setUp() {
        Module testModule = new AbstractModule() {
            @Override
            public void configure() {
            }
        };
        GuiceApplicationBuilder builder = new GuiceApplicationLoader()
                .builder(new ApplicationLoader.Context(Environment.simple()))
                .overrides(testModule);
        Guice.createInjector(builder.applicationModule()).injectMembers(this);
        Helpers.start(application);
    }

    /**
     * Stop the backend server
     */
    @After("@LoginSteps")
    public void tearDown() {
        Helpers.stop(application);
    }

    @Given("that I have signed up successfully with valid data:")
    public void thatIHaveSignedUpSuccessfullyWithValidData(DataTable dataTable) {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        this.userData = Json.toJson(firstRow);
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/users/signup")
                .bodyJson(this.userData);
        this.signUpResponse = route(application, request);
        Assert.assertEquals(201, this.signUpResponse.status());
    }


    @When("I write correct login credentials in the Login form and I click the Login button")
    public void iWriteCorrectLoginCredentialsinTheLoginFormAndIClickTheLoginButton() {
        // Gets the user credentials from the initial data
        String email = this.userData.get("email").asText();
        String password = this.userData.get("password").asText();

        // Constructing the request body
        ObjectNode reqJsonBody = Json.newObject();
        reqJsonBody.put("email", email);
        reqJsonBody.put("password", password);

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/users/login")
                .bodyJson(reqJsonBody);
        this.loginResponse = route(application, request);
        Assert.assertTrue(!(this.loginResponse == null));
    }

    @Then("the response should have an authentication token")
    public void theResponseShouldHaveAnAuthenticationToken() throws IOException {
        JsonNode authenticationResponseAsJson = PlayResultToJson.convertResultToJson(this.loginResponse);
        String authToken = authenticationResponseAsJson.get("token").asText();

        Assert.assertTrue(authToken.length() > 0);
    }

    @When("I write incorrect login credentials in the Login form and I click the Login button")
    public void iWriteIncorrectLoginCredentialsinTheLoginFormAndIClickTheLoginButton() {
        // Gets the user credential from the initial data
        String email = this.userData.get("email").asText();
        String password = this.userData.get("password").asText();

        // Constructing the request body
        ObjectNode reqJsonBody = Json.newObject();
        reqJsonBody.put("email", email);
        reqJsonBody.put("password", password + "wrong-password");

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/users/login")
                .bodyJson(reqJsonBody);
        this.loginResponse = route(application, request);
        Assert.assertTrue(!(this.loginResponse == null));
    }

    @Then("the server should not log me in")
    public void theServerShouldNotLogMeIn() {
        Assert.assertEquals(401, this.loginResponse.status());
    }
}
