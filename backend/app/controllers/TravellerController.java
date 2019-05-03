package controllers;

import actions.ActionState;
import actions.Admin;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigException;
import models.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.TravellerRepository;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.text.SimpleDateFormat;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import play.libs.concurrent.HttpExecutionContext;
import util.Security;

/**
 * Contains all endpoints associated with travellers
 */
public class TravellerController extends Controller {
    private final TravellerRepository travellerRepository;
    private HttpExecutionContext httpExecutionContext;
    private final Security security;

    @Inject
    public TravellerController(TravellerRepository travellerRepository, HttpExecutionContext httpExecutionContext, Security security) {
        this.travellerRepository = travellerRepository;
        this.httpExecutionContext = httpExecutionContext;
        this.security = security;
    }

    /**
     * Retrieves a travellers details
     * @param travellerId the traveller Id of the traveller to retrieve
     * @param request request Object
     * @return traveller details as a Json object
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getTraveller(int travellerId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);

        CompletionStage<Optional<User>> getUser;

        if (user.getUserId() == travellerId) {
           getUser = travellerRepository.getUserById(travellerId, true);
        } else {
            getUser = travellerRepository.getUserById(travellerId, false);
        }

        return getUser
                .thenApplyAsync((optUser) -> {
                    if (!optUser.isPresent()) {
                        return notFound();
                    }

                    JsonNode userAsJson = Json.toJson(optUser.get());

                    return ok(userAsJson);

                }, httpExecutionContext.current());

    }

    /**
     * Updates a travellers details
      * @param travellerId Redundant ID
     * @param request Object to get the JSOn data
     * @return 200 status if update was successful, 500 otherwise
     */
    @With({LoggedIn.class})
    public CompletionStage<Result> updateTraveller(int travellerId, Http.Request request) {
        JsonNode jsonBody = request.body().asJson();
        User userFromMiddleware = request.attrs().get(ActionState.USER);

        if (!security.userHasPermission(userFromMiddleware, travellerId)) {
            return supplyAsync(() -> unauthorized());
        }

        return travellerRepository.getUserById(travellerId, false)
                .thenApplyAsync((user) -> {
                    if (!user.isPresent()) {
                        return notFound();
                    }

                    if (jsonBody.has("firstName")) {
                        user.get().setFirstName(jsonBody.get("firstName").asText());
                    }

                    if (jsonBody.has("middleName")) {
                        user.get().setMiddleName(jsonBody.get("middleName").asText());
                    }

                    if (jsonBody.has("lastName")) {
                        user.get().setLastName(jsonBody.get("lastName").asText());
                    }

                    if (jsonBody.has("dateOfBirth")) {
                        try {
                            String incomingDate = jsonBody.get("dateOfBirth").asText();
                            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(incomingDate);
                            System.out.println("Date stored in db for user is: " + date);
                            user.get().setDateOfBirth(date);
                        } catch (ParseException e) {
                            System.out.println(e);
                        }
                    }

                    if (jsonBody.has("gender")) {
                        user.get().setGender(jsonBody.get("gender").asText());
                    }

                    if (jsonBody.has("nationalities")) {
                        JsonNode arrNode = jsonBody.get("nationalities");
                        ArrayList<Nationality> nationalities = new ArrayList<>();
                        for (JsonNode id : arrNode) {

                            Nationality nationality = new Nationality(null);
                            nationality.setNationalityId(id.asInt());
                            nationalities.add(nationality);

                        }
                        user.get().setNationalities(nationalities);
                    }

                    if (jsonBody.has("passports")) {
                        JsonNode arrNode = jsonBody.get("passports");
                        ArrayList<Passport> passports = new ArrayList<>();
                        for (JsonNode id : arrNode) {

                            Passport passport = new Passport(null);
                            passport.setPassportId(id.asInt());
                            passports.add(passport);

                        }
                        user.get().setPassports(passports);
                    }

                    if (jsonBody.has("travellerTypes")) {
                        JsonNode arrNode = jsonBody.get("travellerTypes");
                        ArrayList<TravellerType> travellerTypes = new ArrayList<>();
                        for (JsonNode id : arrNode) {
                            TravellerType travellerType = new TravellerType(null);
                            travellerType.setTravellerTypeId(id.asInt());
                            travellerTypes.add(travellerType);
                        }
                        user.get().setTravellerTypes(travellerTypes);
                    }

                    if (jsonBody.has("gender")) {
                        user.get().setGender(jsonBody.get("gender").asText());
                    }

                    ObjectNode message = Json.newObject();
                    message.put("message", "Successfully updated the traveller's information");

                    travellerRepository.updateUser(user.get());
                    return ok(message);

                }, httpExecutionContext.current());


//        if (jsonBody.has("firstName")) {
//            user.setFirstName(jsonBody.get("firstName").asText());
//        }
//
//        if (jsonBody.has("middl all other admins should be able to delete each othereName")) {
//            user.setMiddleName(jsonBody.get("middleName").asText());
//        }
//
//        if (jsonBody.has("lastName")) {
//            user.setLastName(jsonBody.get("lastName").asText());
//        }
//
//        if (jsonBody.has("dateOfBirth")) {
//            try {
//                String incomingDate = jsonBody.get("dateOfBirth").asText();
//                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(incomingDate);
//                System.out.println("Date stored in db for user is: " + date);
//                user.setDateOfBirth(date);
//            } catch (ParseException e) {
//                System.out.println(e);
//            }
//        }
// all other admins should be able to delete each other
//        if (jsonBody.has("gender")) {
//            user.setGender(jsonBody.get("gender").asText());
//        }
//
//        if (jsonBody.has("nationalities")) {
//            JsonNode arrNode = jsonBody.get("nationalities");
//            ArrayList<Nationality> nationalities = new ArrayList<>();
//            for (JsonNode id : arrNode) {
//
//                Nationality nationality = new Nationality(null);
//                nationality.setNationalityId(id.asInt());
//                nationalities.add(nationality);
//
//            }
//            user.setNationalities(nationalities);
//        }
//
//        if (jsonBody.has("passports")) {
//            JsonNode arrNode = jsonBody.get("passports");
//            ArrayList<Passport> passports = new ArrayList<>();
//            for (JsonNode id : arrNode) {
//
//                Passport passport = new Passport(null);
//                passport.setPassportId(id.asInt());
//                passports.add(passport);
//
//            }
//            user.setPassports(passports);
//        }
//
//       if (jsonBody.has("travellerTypes")) {
//            JsonNode arrNode = jsonBody.get("travellerTypes");
//            ArrayList<TravellerType> travellerTypes = new ArrayList<>();
//            for (JsonNode id : arrNode) {
//                TravellerType travellerType = new TravellerType(null);
//                travellerType.setTravellerTypeId(id.asInt());
//                travellerTypes.add(travellerType);
//            }
//            user.setTravellerTypes(travellerTypes);
//        }
//
//        if (jsonBody.has("gender")) {
//            user.setGender(jsonBody.get("gender").asText());
//        }
//
//        ObjectNode message = Json.newObject();
//        message.put("message", "Successfully updated the traveller's information");
//
//        return supplyAsync(() -> {
//            travellerRepository.updateUser(user);
//            return ok(message);
//        }, httpExecutionContext.current());
    }

    /**
     * A function that gets a list of all the passports and returns a 200 ok code to the HTTP client
     * @param request Http.Request the HTTP request
     * @return a status code 200 if the request is successful, otherwise returns 500.
     */
    public CompletionStage<Result> getAllPassports(Http.Request request) {
        return travellerRepository.getAllPassports()
                .thenApplyAsync((passports) -> {
                    return ok(Json.toJson(passports));
                }, httpExecutionContext.current());
    }


    /**
     * Gets a list of all the nationalities and returns it with a 200 ok code to the HTTP client
     * @param request <b>Http.Request</b> the http request
     * @return <b>CompletionStage&ltResult&gt</b> the completion function to be called on completion
     */
    public CompletionStage<Result> getNationalities(Http.Request request) {
        return travellerRepository.getAllNationalities()
                .thenApplyAsync((nationalities) -> {
                    return ok(Json.toJson(nationalities));
                }, httpExecutionContext.current());
    }


    @With(LoggedIn.class)
    public CompletionStage<Result> getTravellers() {
        return travellerRepository.getTravellers()
                .thenApplyAsync(travellers -> {
                    JsonNode travellersJson = Json.toJson(travellers);
                    return ok(travellersJson);
                }, httpExecutionContext.current());
    }


    /**
     * Retrieve user roles from reqquest body and update the specified user so they
     * have these roles.
     * @param travellerId user to have roles updated
     * @param request HTTP request
     * @return a status code of 200 is ok, 400 if user or role doesn't exist
     */
    @With({LoggedIn.class, Admin.class})
    public CompletionStage<Result> updateTravellerRole(int travellerId, Http.Request request) {

        // Check travellerID isn't a super admin already
        // Check the patch doesn't give someone a super admin role

        JsonNode jsonBody = request.body().asJson();
        JsonNode roleArray = jsonBody.withArray("roleTypes");
        List<String> roleTypes = new ArrayList<>();


        for (JsonNode roleJson : roleArray) {
            String roleTypeString = roleJson.asText();
            if (!RoleType.contains(roleTypeString)) {
                return supplyAsync(() -> badRequest());
            }
            roleTypes.add(roleTypeString);
        }

        return travellerRepository.getUserById(travellerId, false)
                .thenApplyAsync(optionalUser -> {
                    if (!optionalUser.isPresent()) {
                        return notFound();
                    }
                    List<Role> userRoles = travellerRepository.getRolesByRoleType(roleTypes);
                    User user = optionalUser.get();

                    // Prevent a super admin from having their permission removed
                    if (user.isSuperAdmin()) {
                        boolean flag = false;
                        for (String roleString : roleTypes) {
                            if (roleString.equals(RoleType.SUPER_ADMIN.name())) {
                                flag = true;
                            }
                        }
                        if (!flag) { return forbidden(); }
                    } else {
                        // Prevents a non super admin from getting super-admin permission
                        for (String roleString : roleTypes) {
                            if (roleString.equals(RoleType.SUPER_ADMIN.name())) {
                                return forbidden();
                            }
                        }
                    }
                    user.setRoles(userRoles);
                    user.update();
                    return ok("Success");
                });
    }


    /**
     * A function that adds a passport to a user based on the given user ID
     * @param travellerId the traveller ID
     * @param request Object to get the passportId to add
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> addPassport(int travellerId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);


        int passportId = request.body().asJson().get("passportId").asInt();

        return travellerRepository.getPassportById(passportId)
                .thenApplyAsync((passport) -> {
                    if (!passport.isPresent()) {
                        return notFound();
                    }
                    List<Passport> passports = user.getPassports();
                    passports.add(passport.get());
                    user.setPassports(passports);
                    user.save();
                    return ok();
                }, httpExecutionContext.current());
    }


    /**
     * A function that deletes a passport from a user based on the given user ID
     * @param travellerId the traveller ID
     * @param passportId the passport ID
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> removePassport(int travellerId, int passportId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);

        return travellerRepository.getPassportById(passportId)
                .thenApplyAsync((passport) -> {
                    if (!passport.isPresent()) {
                        return notFound();
                    }
                    List<Passport> passports = user.getPassports();
                    passports.remove(passport.get());
                    user.setPassports(passports);
                    user.save();
                    System.out.println(user.getPassports());
                    return ok();
                }, httpExecutionContext.current());
    }


    /**
     * A function that adds a nationality to the user based on the user ID given
     * @param travellerId the traveller ID
     * @param request Object to get the nationality to add.
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> addNationality(int travellerId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        int nationalityId = request.body().asJson().get("nationalityId").asInt();

        return travellerRepository.getNationalityById(nationalityId)
                .thenApplyAsync((nationality) -> {
                    if (!nationality.isPresent()) {
                        return notFound();
                    }
                    List<Nationality> nationalities = user.getNationalities();
                    nationalities.add(nationality.get());
                    user.setNationalities(nationalities);
                    user.save();
                    return ok();
                }, httpExecutionContext.current());
    }


    /**
     * Deletes a nationality for a logged in user given a nationality id in the request body
     * @param travellerId the traveller for which we want to delete the nationality
     * @param request the request passed by the routes file
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> deleteNationalityForUser(int travellerId, int nationalityId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return travellerRepository.getNationalityById(nationalityId)
                .thenApplyAsync((optionalNationality) -> {
                    if (!optionalNationality.isPresent()) {
                        return notFound("Could not find nationality " + nationalityId);
                    }
                    // now that we know that the nationality definitely exists
                    // extract the Nationality from the Optional<Nationality> object
                    Nationality nationality = optionalNationality.get();
                    List<Nationality> userNationalities = user.getNationalities();

                    // return not found if the user did not have that nationality already
                    if (!userNationalities.contains(nationality)) {
                        return notFound("User does not have nationality " + nationalityId);
                    }

                    userNationalities.remove(nationality);
                    user.setNationalities(userNationalities);
                    user.save();
                    return ok("Successfully deleted nationality");
                }, httpExecutionContext.current());
    }

    /**
     * Get a list of all valid traveller types
     * @param request unused request object
     * @return ok with status 200 if types obtained, 401 if no token is provided
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getTravellerTypes(Http.Request request) {
        return travellerRepository.getAllTravellerTypes()
                .thenApplyAsync((types) -> {
                    return ok(Json.toJson(types));
                }, httpExecutionContext.current());
    }



    /**
     * Allows the front-end to search for a traveller.
     * @param request
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> searchTravellers(Http.Request request) {
        int nationality;
        nationality = -1;
        int travellerType;
        travellerType = -1;
        long ageMin;
        ageMin = -1;
        long ageMax;
        ageMax = -1;
        String gender = "";
        try {
            String nationalityQuery = request.getQueryString("nationality");
            if (!nationalityQuery.isEmpty())
                nationality = Integer.parseInt(nationalityQuery);
        } catch (Exception e){ System.out.println("No Parameter nationality");}
        try {
            String ageMinQuery = request.getQueryString("ageMin");
            if (!ageMinQuery.isEmpty())
                ageMin = Long.parseLong(ageMinQuery);
        } catch (Exception e){ System.out.println("No Parameter ageMin");}
        try {
            String ageMaxQuery = request.getQueryString("ageMax");
            if(!ageMaxQuery.isEmpty())
                ageMax = Long.parseLong(ageMaxQuery);
        } catch (Exception e){ System.out.println("No Parameter ageMax");}
        try {
            String travellerTypeQuery = request.getQueryString("travellerType");
            if (!travellerTypeQuery.isEmpty())
                travellerType = Integer.parseInt(travellerTypeQuery);
        } catch (Exception e){ System.out.println("No Parameter travellerType");}
        try {
            gender = request.getQueryString("gender");
        } catch (Exception e){ System.out.println("No Parameter gender");}
        Date dateMin = new Date(ageMin);
        Date dateMax = new Date(ageMax);

        System.out.println("nationality="+nationality + " agemin=" + ageMin +" agemax="+ ageMax + " gender=" + gender + " travellerType=" + travellerType);

        return travellerRepository.searchUser(nationality,gender,dateMin,dateMax,travellerType)  //Just for testing purposes
                .thenApplyAsync((user) -> {
                    JsonNode userAsJson = Json.toJson(user);
                    System.out.println(userAsJson);

                    return ok(userAsJson);

                }, httpExecutionContext.current());

    }

}
