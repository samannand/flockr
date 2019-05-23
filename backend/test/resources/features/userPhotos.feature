Feature: As a registered user I want to have photos that display on my profile.

# NOTE: please leave the @UserPhotos tags in here for tests that save photos to the hard drive as
# this is used in the tear down to delete them.

  Background:
    Given users with the following information exist:
      | firstName | lastName   | email                  | password   |
      | Phillip   | Phototaker | ppt@phillipsphotos.com | say-cheese |
      | Angelo    | Admin      | aa@admin.com           | its-ya-boi |

  @UserPhoto
  Scenario: a user wants to view all of their photos
    Given the user has the following photos in the system:
      | filename      | isPrimary | isPublic |
      | monkey.png    | true      | false    |
      | dog.jpg       | false     | false    |
      | cat.jpeg      | false     | false    |
      | cucumber.jpeg | false     | false    |
      | whale.png     | false     | false    |
    When the user requests all their photos
    Then the user gets the same list

  @UserPhotos
  Scenario: a user requests a list of photos for a user that does not exist
    When a user requests photos for a non signed up userId
    Then they should receive a "Not Found" error message with a 404 error code

  @UserPhotos
  Scenario: a user that has not signed in requests a list of their photos
    When a non signed in user requests photos for another user
    Then they should receive a "Unauthorized" error message with a 401 error code

  @UserPhotos
  Scenario: an admin user requests the photos of another user
    Given one of the users is an admin
    Given the user has the following photos in the system:
      | filename      | isPrimary | isPublic |
      | monkey.png    | true      | false    |
      | dog.jpg       | false     | false    |
      | cat.jpeg      | false     | false    |
      | cucumber.jpeg | false     | false    |
      | whale.png     | false     | false    |
    When the admin user requests the photos of another user
    Then the user gets the same list

  @UserPhotos
  Scenario: A user can add a photo
    Given a user has a photo called "dog.jpg"
    And they want the photo to be public
    And they want the photo to be their profile photo
    When they add the photo
    Then the photo is added

  @UserPhotos
  Scenario: A user wants to get a thumbnail of a photo they do not have
    When the user requests the thumbnail for a non existent photo
    Then they should receive a "Not Found" error message with a 404 error code

  @UserPhotos
  Scenario: A not logged in user wants to get a thumbnail of a photo they have
    Given a user has a photo called "cucumber.jpeg" already
    When the user requests the thumbnail for a photo without their token
    Then they should receive a "Unauthorized" error message with a 401 error code

  @UserPhotos
  Scenario: A user wants to get a thumbnail of a photo they have
    Given a user has a photo called "cucumber.jpeg" already
    When the user requests the thumbnail for this photo
    Then the thumbnail is returned in the response

  @UserPhoto
  Scenario: An admin wants to change a user's photo permission from public to private
    Given a user has a photo called "cucumber.jpeg" already
    And one of the users is an admin
    When the admin changes the photo permission to private
    Then the photo permission is set to private

  @UserPhoto
  Scenario: A user wants to change their photo permission from public to private
    Given a user has a photo called "cucumber.jpeg" already
    When the user changes the photo permission to private
    Then the photo permission is set to private

  @UserPhoto
  Scenario: A user wants to change another user's photo permission from public to private
    Given a user has a photo called "cucumber.jpeg" already
    When another user changes the photo permission to private
    Then that user is not allowed to change the photo permission

  @UserPhoto
  Scenario: A user wants to change a photo permission of a photo that does not exist from public to private
    Given a user has no photo with the photo id 1000
    When the user changes the photo permission of the non-existent photo to private
    Then the photo cannot be found

  @UserPhotos
  Scenario: A user wants to delete a photo that they have
    Given a user has a photo called "cucumber.jpeg" already
    When the user requests that the photo be deleted
    Then the photo is deleted

  @UserPhotos
  Scenario: A logged out user wants to get a photo
    Given a user has a photo called "cucumber.jpeg" already
    When the logged out user requests the photo
    Then they should receive a "Unauthorized" error message with a 401 error code

  @UserPhotos
  Scenario: A logged in user wants to get one of their photos
    Given a user has a photo called "cucumber.jpeg" already
    When the user requests the photo
    Then the photo is returned in the response body with a status of 200

  @UserPhotos
  Scenario: A logged in user wants to get a picture of another user
    Given a user has a photo called "cucumber.jpeg" already
    When the logged in user requests the photo
    Then the photo is returned in the response body with a status of 200

  @UserPhotos
  Scenario: A logged in user wants to get a private picture of another user
    Given a user has a private photo called "cucumber.jpeg" already
    When the logged in user requests the photo
    Then they should receive a "Forbidden" error message with a 403 error code

  @UserPhotos
  Scenario: A logged in user wants to get a photo that does not exist
    When no user has a photo with id 1337
    Then they should receive a "Not Found" error message with a 404 error code

  @UserPhotos
  Scenario: An admin user wants to get a private photo of another user
    Given a user has a private photo called "cucumber.jpeg" already
    When the admin user requests the photo
    Then the photo is returned in the response body with a status of 200

  @UserPhotos
  Scenario: A user wants to delete a photo that they do not have
    Given a user has a photo called "cucumber.jpeg" already
    When another user requests that the photo be deleted
    Then they should receive a "Forbidden" error message with a 403 error code