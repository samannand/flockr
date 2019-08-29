package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import gherkin.deps.com.google.gson.JsonObject;
import models.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;
import utils.FakeClient;
import utils.FakePlayClient;
import utils.PlayResultToJson;
import utils.TestState;

import javax.swing.plaf.LabelUI;
import java.io.IOException;
import java.util.*;

public class ChatControllerTest {
  private Application application;
  private User user;
  private User otherUser;
  private User adminUser;
  private User anotherUser;
  private FakeClient fakeClient;
  private ChatGroup chatGroup;
  private ChatGroup chatGroup2;
  private ChatGroup chatGroup3;


  @Before
  public void setUp() throws IOException, ServerErrorException, FailedToSignUpException {
    Map<String, String> testSettings = new HashMap<>();
    testSettings.put("db.default.driver", "org.h2.Driver");
    testSettings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");
    testSettings.put("play.evolutions.db.default.enabled", "true");
    testSettings.put("play.evolutions.db.default.autoApply", "true");
    testSettings.put("play.evolutions.db.default.autoApplyDowns", "true");

    application = Helpers.fakeApplication(testSettings);
    Helpers.start(application);

    TestState.getInstance().setApplication(application);
    TestState.getInstance().setFakeClient(new FakePlayClient(application));

    fakeClient = TestState.getInstance().getFakeClient();
    user = fakeClient.signUpUser("Timmy", "Tester", "timmy@tester.com",
            "abc123");
    otherUser = fakeClient.signUpUser("Tammy", "Tester", "tammy@tester.com",
            "abc123");
    adminUser = fakeClient.signUpUser("Andy", "Admin", "andy@admin.com",
            "abc123");
    anotherUser = fakeClient.signUpUser("Sam", "IsAwesome", "sam@theman.com", "abc123");

    Role role = new Role(RoleType.ADMIN);
    List<Role> roles = new ArrayList<>();
    roles.add(role);
    role.save();
    user.save();
    adminUser = User.find.byId(adminUser.getUserId());
    adminUser.setRoles(roles);
    adminUser.save();
    otherUser.save();
    anotherUser.save();

    List<User> usersInChat = new ArrayList<>();
    usersInChat.add(user);
    usersInChat.add(otherUser);
    chatGroup = new ChatGroup("my chat", usersInChat, new ArrayList<>());
    chatGroup.save();

    List<User> usersInChat2 = new ArrayList<>();
    usersInChat2.add(adminUser);
    usersInChat2.add(otherUser);
    chatGroup2 = new ChatGroup("my chat2", usersInChat2, new ArrayList<>());
    chatGroup2.save();

    List<User> usersInChat3 = new ArrayList<>();
    usersInChat3.add(user);
    usersInChat3.add(otherUser);
    chatGroup3 = new ChatGroup("Testing Messages", usersInChat3, new ArrayList<>());
    chatGroup3.save();

    // Send 30 messages to chat group 3
    for (int i = 0; i < 30; i++) {
      Message message = new Message(chatGroup3, "Test Message " + (i + 1), user);
      message.save();
    }

  }

  @After
  public void tearDown() {
    Application application = TestState.getInstance().getApplication();
    Helpers.stop(application);
    TestState.clear();
  }

  @Test
  public void shouldCreateChatWithValidData() throws IOException {
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.put("name", "my chat name");
    chatGroupBody.putArray("userIds")
           .add(otherUser.getUserId());

    String endpoint = "/api/chats";
    Result result = fakeClient.makeRequestWithToken("POST", chatGroupBody, endpoint, adminUser.getToken()) ;;

    Assert.assertEquals(201, result.status());
    JsonNode chatGroupResBody = PlayResultToJson.convertResultToJson(result);

    // Make sure json contains the right fields
    Assert.assertTrue(chatGroupResBody.has("chatGroupId"));
    Assert.assertTrue(chatGroupResBody.has("name"));
    Assert.assertTrue(chatGroupResBody.has("users"));
    Set<Integer> usersInChat = getUsersInChat(chatGroupResBody.get("users"));
    Assert.assertTrue(usersInChat.contains(adminUser.getUserId()));
    Assert.assertTrue(usersInChat.contains(otherUser.getUserId()));


    Assert.assertTrue(!chatGroupResBody.has("messages"));

    // Make sure DB contains right data
    ChatGroup chatGroup = ChatGroup.find.byId(chatGroupResBody.get("chatGroupId").asInt());
    Assert.assertNotNull(chatGroup);
    Assert.assertEquals(chatGroup.getUsers().size(), 2);
    Assert.assertEquals(0, chatGroup.getMessages().size());
    Assert.assertEquals(chatGroupResBody.get("chatGroupId").asInt(), chatGroup.getChatGroupId());
  }

  @Test
  public void shouldNotCreateChatWithNoName() throws IOException {
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.putArray("userIds")
           .add(otherUser.getUserId());

    String endpoint = "/api/chats";
    Result result = fakeClient.makeRequestWithToken("POST", chatGroupBody, endpoint, adminUser.getToken()) ;;
    Assert.assertEquals(400, result.status());
  }

  @Test
  public void shouldNotCreateChatWithNoUserIds() throws IOException {
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.put("name", "my chat name");
    String endpoint = "/api/chats";
    Result result = fakeClient.makeRequestWithToken("POST", chatGroupBody, endpoint, adminUser.getToken()) ;;
    Assert.assertEquals(400, result.status());
  }

  @Test
  public void shouldNotCreateChatWhenOwnUserIsSpecified() throws IOException {
    // Should not create chat when own user id specified as the people that should be in the chat
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.put("name", "my chat name");
    chatGroupBody.putArray("userIds")
         .add(adminUser.getUserId());

    String endpoint = "/api/chats";
    Result result = fakeClient.makeRequestWithToken("POST", chatGroupBody, endpoint, adminUser.getToken()) ;;
    Assert.assertEquals(403, result.status());
  }

  @Test
  public void shouldNotCreateChatWhenNoUsersAreSpecified() throws IOException {
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.put("name", "my chat name");

    // Empty array means you aren't in a group with anyone which is invalid
    chatGroupBody.putArray("userIds");
    String endpoint = "/api/chats";
    Result result = fakeClient.makeRequestWithToken("POST", chatGroupBody, endpoint, adminUser.getToken()) ;;
    Assert.assertEquals(400, result.status());
  }

  @Test
  public void shouldNotCreatChatWhenNotLoggedIn() {
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.put("name", "my chat name");
    chatGroupBody.putArray("userIds")
            .add(otherUser.getUserId());

    String endpoint = "/api/chats";
    Result result = fakeClient.makeRequestWithToken("POST", chatGroupBody, endpoint, "random-invalid-token");
    Assert.assertEquals(401, result.status());
  }

  @Test
  public void shouldGetAllChatsForUser() throws IOException {
    String endpoint = "/api/chats";
    Result result = fakeClient.makeRequestWithToken("GET", endpoint, user.getToken());
    JsonNode responseBody = PlayResultToJson.convertResultToJson(result);
    Assert.assertEquals(200, result.status());
    Assert.assertEquals(1, responseBody.size());

    JsonNode chatGroupJson = responseBody.get(0);

    Assert.assertTrue(chatGroupJson.has("name"));

    // Make sure JSON to NOT include messages
    Assert.assertTrue(!chatGroupJson.has("messages"));
    Assert.assertTrue(chatGroupJson.has("users"));
    Set<Integer> usersInChat = getUsersInChat(chatGroupJson.get("users"));

    Assert.assertTrue(usersInChat.contains(otherUser.getUserId()));
    Assert.assertTrue(usersInChat.contains(user.getUserId()));
  }

  /**
   * Converts users json into a set representing the current users ID's
   * @return A set containing the current user ID's in the chat
   */
  public Set<Integer> getUsersInChat(JsonNode usersInChat) {
    Set<Integer> userIds = new HashSet<>();
    for (JsonNode userJson : usersInChat) {
        userIds.add(userJson.get("userId").asInt());
    }

    return userIds;
  }

  @Test
  public void shouldNotGetChatsIfUserIsNotLoggedIn() throws IOException {
    String endpoint = "/api/chats";
    Result result = fakeClient.makeRequestWithToken("GET", endpoint, "some-invalid-token");
    JsonNode responseBody = PlayResultToJson.convertResultToJson(result);
    Assert.assertEquals(401, result.status());
  }

  @Test
  public void shouldSuccessfullyDeleteChatGroup() throws IOException {
    String endpoint = "/api/chats/" + chatGroup.getChatGroupId();
    Result result = fakeClient.makeRequestWithToken("DELETE", endpoint, user.getToken());
    Assert.assertEquals(200, result.status());

    // Make sure that it's been deleted in the DB as well
    Assert.assertNull(ChatGroup.find.byId(chatGroup.getChatGroupId()));
  }

  @Test
  public void shouldNotDeleteChatGroupWhenNotInGroup() {
    String endpoint = "/api/chats/" + chatGroup2.getChatGroupId();
    Result result = fakeClient.makeRequestWithToken("DELETE", endpoint, user.getToken());
    Assert.assertEquals(403, result.status());

    // Make sure that it's still in DB
    Assert.assertNotNull(ChatGroup.find.byId(chatGroup2.getChatGroupId()));
  }

  @Test
  public void shouldNotDeleteChatGroupWhenGroupDoesNotExist() {
    String endpoint = "/api/chats/1234";
    Result result = fakeClient.makeRequestWithToken("DELETE", endpoint, user.getToken());
    Assert.assertEquals(404, result.status());

    // Make sure that it's still in DB
    Assert.assertNotNull(ChatGroup.find.byId(chatGroup.getChatGroupId()));
  }

  @Test
  public void shouldCreateMessageInChat() {
    String endpoint = "/api/chats/" + chatGroup.getChatGroupId() + "/message";
    ObjectNode requestBody = Json.newObject();
    String message = "This is my message";
    requestBody.put("message", message);
    Result result = fakeClient.makeRequestWithToken("POST", requestBody, endpoint, user.getToken());
    Assert.assertEquals(201, result.status());

    // Check that message is actually in the DB
    ChatGroup returnedChatGroup = ChatGroup.find.byId(chatGroup.getChatGroupId());
    Assert.assertEquals(1, returnedChatGroup.getMessages().size());
    Assert.assertEquals(message, returnedChatGroup.getMessages().get(0).getContents());
  }

  @Test
  public void shouldNotCreateMessageIfGroupDoesNotExist() {
    String endpoint = "/api/chats/1234/message";
    ObjectNode requestBody = Json.newObject();
    String message = "This is my message";
    requestBody.put("message", message);
    Result result = fakeClient.makeRequestWithToken("POST", requestBody, endpoint, user.getToken());
    Assert.assertEquals(404, result.status());

    // Check that message is not in the DB
    ChatGroup returnedChatGroup = ChatGroup.find.byId(chatGroup.getChatGroupId());
    Assert.assertEquals(0, returnedChatGroup.getMessages().size());
  }

  @Test
  public void shouldNotCreateMessageIfNotPartOfGroup() {
    String endpoint = "/api/chats/" + chatGroup2.getChatGroupId() + "/message";
    ObjectNode requestBody = Json.newObject();
    String message = "This is my message";
    requestBody.put("message", message);
    Result result = fakeClient.makeRequestWithToken("POST", requestBody, endpoint, user.getToken());
    Assert.assertEquals(403, result.status());

    // Check that message is not in the DB
    ChatGroup returnedChatGroup = ChatGroup.find.byId(chatGroup.getChatGroupId());
    Assert.assertEquals(0, returnedChatGroup.getMessages().size());
  }

   @Test
  public void shouldDeleteMessageInChat() {
    Message message = new Message(chatGroup, "Random message", user);
    message.save();

    String endpoint = "/api/chats/message/" + message.getMessageId();
    Result result = fakeClient.makeRequestWithToken("DELETE", endpoint, user.getToken());
    Assert.assertEquals(200, result.status());

    ChatGroup returnedChatGroup = ChatGroup.find.byId(chatGroup.getChatGroupId());
     // Check that message has actually been deleted
    Assert.assertEquals(0, returnedChatGroup.getMessages().size());
  }

  @Test
  public void shouldNotDeleteMessageIfMessageDoesNotExist() {
    Message message = new Message(chatGroup, "Random message", user);
    message.save();
    String endpoint = "/api/chats/message/1234";
    Result result = fakeClient.makeRequestWithToken("DELETE", endpoint, user.getToken());
    Assert.assertEquals(404, result.status());

    // Check that message is still in the db
    ChatGroup returnedChatGroup = ChatGroup.find.byId(chatGroup.getChatGroupId());
    Assert.assertEquals(1, returnedChatGroup.getMessages().size());
  }

  @Test
  public void shouldNotDeleteMessageIfMessageIsNotYours() {
    // otherUser made the message so user should not be able to delete it
    Message message = new Message(chatGroup, "Random message", otherUser);
    message.save();
    String endpoint = "/api/chats/message/" + message.getMessageId();
    Result result = fakeClient.makeRequestWithToken("DELETE", endpoint, user.getToken());
    Assert.assertEquals(403, result.status());

    // Check that message is still in the db
    ChatGroup returnedChatGroup = ChatGroup.find.byId(chatGroup.getChatGroupId());
    Assert.assertEquals(1, returnedChatGroup.getMessages().size());
  }


  // Edit chat group testing

  @Test
  public void editChatGroupOk() {

    String newChatName = "newChatName";
    List<Integer> newUserIds = new ArrayList<>();
    newUserIds.add(adminUser.getUserId());
    newUserIds.add(user.getUserId());
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.put("name", newChatName);
    chatGroupBody.set("userIds", Json.toJson(newUserIds));

    String endpoint = "/api/chats/" + chatGroup.getChatGroupId();
    Result result = fakeClient.makeRequestWithToken("PUT", chatGroupBody, endpoint, user.getToken());

    Assert.assertEquals(200, result.status());

    ChatGroup modifiedChat = ChatGroup.find.byId(chatGroup.getChatGroupId());

    Set<Integer> chatUserIds = new HashSet<>();
    for (User user : modifiedChat.getUsers()) {
      chatUserIds.add(user.getUserId());
    }
    Set<Integer> expectedUserIds = new HashSet<>(newUserIds);
    Assert.assertEquals(expectedUserIds, chatUserIds);

  }

  @Test
  public void editChatGroupUnauthorized() {

    String newChatName = "newChatName";
    List<Integer> newUserIds = new ArrayList<>();
    newUserIds.add(adminUser.getUserId());
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.put("name", newChatName);
    chatGroupBody.set("userIds", Json.toJson(newUserIds));

    String endpoint = "/api/chats/" + chatGroup.getChatGroupId();
    Result result = fakeClient.makeRequestWithNoToken("PUT", chatGroupBody, endpoint);

    Assert.assertEquals(401, result.status());

    ChatGroup unmodifiedChat = ChatGroup.find.byId(chatGroup.getChatGroupId());

    Set<Integer> chatUserIds = new HashSet<>();
    for (User user : unmodifiedChat.getUsers()) {
      chatUserIds.add(user.getUserId());
    }
    Set<User> expectedUsers = new HashSet<>(chatGroup.getUsers());
    Assert.assertEquals(expectedUsers, new HashSet<>(unmodifiedChat.getUsers()));

  }

  @Test
  public void editChatGroupNotFound() {

    String endpoint =  "/api/chats/31415926";
    Result result = fakeClient.makeRequestWithToken("PUT", endpoint, user.getToken());
    Assert.assertEquals(404, result.status());

  }

  @Test
  public void editChatGroupForbiddenNotInGroup() {

    String newChatName = "newChatName";
    List<Integer> newUserIds = new ArrayList<>();
    newUserIds.add(adminUser.getUserId());
    newUserIds.add(user.getUserId());
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.put("name", newChatName);
    chatGroupBody.set("userIds", Json.toJson(newUserIds));

    String endpoint = "/api/chats/" + chatGroup.getChatGroupId();
    Result result = fakeClient.makeRequestWithToken("PUT", chatGroupBody, endpoint, anotherUser.getToken());

    Assert.assertEquals(403, result.status());

    ChatGroup unmodifiedChat = ChatGroup.find.byId(chatGroup.getChatGroupId());

    Set<Integer> chatUserIds = new HashSet<>();
    for (User user : unmodifiedChat.getUsers()) {
      chatUserIds.add(user.getUserId());
    }
    Set<User> expectedUsers = new HashSet<>(chatGroup.getUsers());
    Assert.assertEquals(expectedUsers, new HashSet<>(unmodifiedChat.getUsers()));

  }

  @Test
  public void editChatGroupForbiddenDuplicateUsers() {
    String newChatName = "newChatName";
    List<Integer> newUserIds = new ArrayList<>();
    newUserIds.add(adminUser.getUserId());
    newUserIds.add(adminUser.getUserId());
    newUserIds.add(user.getUserId());
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.put("name", newChatName);
    chatGroupBody.set("userIds", Json.toJson(newUserIds));

    String endpoint = "/api/chats/" + chatGroup.getChatGroupId();
    Result result = fakeClient.makeRequestWithToken("PUT", chatGroupBody, endpoint, user.getToken());

    Assert.assertEquals(403, result.status());

    ChatGroup unmodifiedChat = ChatGroup.find.byId(chatGroup.getChatGroupId());

    Set<Integer> chatUserIds = new HashSet<>();
    for (User user : unmodifiedChat.getUsers()) {
      chatUserIds.add(user.getUserId());
    }
    Set<User> expectedUsers = new HashSet<>(chatGroup.getUsers());
    Assert.assertEquals(expectedUsers, new HashSet<>(unmodifiedChat.getUsers()));
  }

  @Test
  public void editChatGroupAdmin() {
    String newChatName = "editingAsAnAdmin";
    List<Integer> newUserIds = new ArrayList<>();
    newUserIds.add(adminUser.getUserId());
    newUserIds.add(user.getUserId());
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.put("name", newChatName);
    chatGroupBody.set("userIds", Json.toJson(newUserIds));

    String endpoint = "/api/chats/" + chatGroup.getChatGroupId();
    Result result = fakeClient.makeRequestWithToken("PUT", chatGroupBody, endpoint, adminUser.getToken());

    Assert.assertEquals(200, result.status());

    ChatGroup modifiedChat = ChatGroup.find.byId(chatGroup.getChatGroupId());

    Set<Integer> chatUserIds = new HashSet<>();
    for (User user : modifiedChat.getUsers()) {
      chatUserIds.add(user.getUserId());
    }
    Set<Integer> expectedUserIds = new HashSet<>(newUserIds);
    Assert.assertEquals(expectedUserIds, chatUserIds);
  }

  @Test
  public void editChatGroupBadRequest() {
    String newChatName = "newChatName";
    List<Integer> newUserIds = new ArrayList<>();
    newUserIds.add(adminUser.getUserId());
    newUserIds.add(user.getUserId());
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.put("names", newChatName);
    chatGroupBody.set("users", Json.toJson(newUserIds));

    String endpoint = "/api/chats/" + chatGroup.getChatGroupId();
    Result result = fakeClient.makeRequestWithToken("PUT", chatGroupBody, endpoint, user.getToken());

    Assert.assertEquals(400, result.status());

    ChatGroup unmodifiedChat = ChatGroup.find.byId(chatGroup.getChatGroupId());

    Set<Integer> chatUserIds = new HashSet<>();
    for (User user : unmodifiedChat.getUsers()) {
      chatUserIds.add(user.getUserId());
    }
    Set<User> expectedUsers = new HashSet<>(chatGroup.getUsers());
    Assert.assertEquals(expectedUsers, new HashSet<>(unmodifiedChat.getUsers()));
  }

  // Get Messages Endpoint Testing

  @Test
  public void getChatMessagesNoParamsOk() {
  }

  @Test
  public void getChatMessagesOffsetOk() {
  }

  @Test
  public void getChatMessagesLimitOk() {
  }

  @Test
  public void getChatMessagesOffsetAndLimitOk() {
  }

  @Test
  public void getChatMessagesUnauthorized() {
  }

  @Test
  public void getChatMessagesForbidden() {
  }

  @Test
  public void getChatMessagesNotFound() {
  }

  @Test
  public void getChatMessagesAdmin() {
  }


}
