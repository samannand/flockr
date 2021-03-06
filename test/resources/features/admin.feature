Feature: The admin user can do any task that is available within the system

  Scenario: A traveller user wants to check what roles it has
    Given A traveller user exists
    And The user is logged in
    When The user requests its roles
    Then A list of roles is returned containing the "TRAVELLER" role


  Scenario: An admin user wants to check what roles it has
    Given An admin user exists
    And The user is logged in
    When The user requests its roles
    Then A list of roles is returned containing the "ADMIN" role

  Scenario: An admin user wants to check what roles another user has
    Given An admin user and another user exists
    And The user is logged in
    When The user requests the roles of another user
    Then A list of roles is returned containing the "TRAVELLER" role

  Scenario: A traveller user wants to get a list of roles available on the system
    Given A traveller user exists
    And The user is logged in
    When The user requests the roles available on the system
    Then A list of roles is returned containing all the roles

  Scenario: An admin user wants to get a list of roles available on the system
    Given An admin user exists
    And The user is logged in
    When The user requests the roles available on the system
    Then A list of roles is returned containing all the roles

  Scenario: An admin user wants to revoke the session of a logged in user
    Given An admin user and another user exists
    And The user is logged in
    When The admin user tries to log out the other user
    Then The session token of the other user is removed from the database
