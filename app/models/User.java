package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.Index;
import io.ebean.annotation.SoftDelete;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import play.data.validation.Constraints;

/**
 * A traveller, who may wish to create trips, go to destinations, book hotels, book flights, etc
 */
@Entity
public class User extends Model {

  /**
   * This is required by EBean to make queries on the database
   */
  public static final Finder<Integer, User> find = new Finder<>(User.class);

  @ManyToMany(mappedBy = "users", cascade = CascadeType.ALL)
  public List<Nationality> nationalities;

  @ManyToMany(mappedBy = "users", cascade = CascadeType.ALL)
  public List<TravellerType> travellerTypes;

  @ManyToMany(mappedBy = "users", cascade = CascadeType.ALL)
  public List<Passport> passports;

  @ManyToMany(mappedBy = "users", cascade = CascadeType.ALL)
  public List<Role> roles;

  @Index(unique = true)
  @Id
  @Constraints.Required
  private int userId;
  @Constraints.Required
  private String firstName;
  private String middleName;
  @Constraints.Required
  private String lastName;
  private Date dateOfBirth;
  private String gender;

  @Constraints.Required
  @Column(unique = true)
  private String email;

  @ManyToMany(mappedBy = "users", cascade = CascadeType.ALL)
  private List<TripComposite> trips;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private List<PersonalPhoto> personalPhotos;

  @OneToOne(cascade = CascadeType.ALL)
  private PersonalPhoto profilePhoto;

  @OneToOne(cascade = CascadeType.ALL)
  private PersonalPhoto coverPhoto;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
  private List<TreasureHunt> treasureHunt;

  @ManyToMany(cascade = CascadeType.ALL)
  private List<ChatGroup> chatGroups;

  @Constraints.Required
  @CreatedTimestamp
  @Column(updatable = false)
  private java.sql.Timestamp timestamp;

  @JsonIgnore
  @Constraints.Required
  private String passwordHash;
  private String token;

  @JsonIgnore
  @SoftDelete
  @Column(name = "deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
  private boolean deleted;

  @JsonIgnore
  private Timestamp deletedExpiry;

  /**
   * Create a new traveller
   *
   * @param firstName the traveller's first name
   * @param middleName the traveller's middle name
   * @param lastName the traveller's last name
   * @param passwordHash the traveller's hashed password
   * @param gender the traveller's gender
   * @param email the traveller's email address
   * @param nationalities the traveller's nationalities
   * @param dateOfBirth the traveller's birthday
   * @param passports the traveller's passports
   * @param token the traveller's token
   */
  public User(
      String firstName,
      String middleName,
      String lastName,
      String passwordHash,
      String gender,
      String email,
      List<Nationality> nationalities,
      List<TravellerType> travellerTypes,
      Date dateOfBirth,
      List<Passport> passports,
      List<Role> roles,
      String token) {
    this.firstName = firstName;
    this.middleName = middleName;
    this.lastName = lastName;
    this.passwordHash = passwordHash;
    this.gender = gender;
    this.email = email;
    this.nationalities = nationalities;
    this.travellerTypes = travellerTypes;
    this.dateOfBirth = dateOfBirth;
    this.passports = passports;
    this.roles = roles;
    this.token = token;
  }

  public User() {

  }

  /**
   * Constructor used for signin up
   *
   * @param firstName Traveller's first name
   * @param lastName Traveller's last name
   * @param email Traveller's email
   * @param passwordHash Traveller's hashed password
   * @param token Traveller's token
   */
  public User(
      String firstName,
      String middleName,
      String lastName,
      String email,
      String passwordHash,
      String token) {
    this.firstName = firstName;
    this.middleName = middleName;
    this.lastName = lastName;
    this.email = email;
    this.passwordHash = passwordHash;
    this.token = token;
  }

  @Override
  public String toString() {
    return "User{"
        + "userId="
        + userId
        + ", firstName='"
        + firstName
        + '\''
        + ", middleName='"
        + middleName
        + '\''
        + ", lastName='"
        + lastName
        + '\''
        + ", nationalities="
        + nationalities
        + ", dateOfBirth="
        + dateOfBirth
        + ", gender='"
        + gender
        + '\''
        + ", email='"
        + email
        + '\''
        + ", travellerTypes="
        + travellerTypes
        + ", passports="
        + passports
        + ", roles="
        + roles
        + ", timestamp="
        + timestamp
        + ", passwordHash='"
        + passwordHash
        + '\''
        + ", token='"
        + token
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof User)) {
      return false;
    }
    User otherUser = (User) o;
    return this.getUserId() == otherUser.getUserId();
  }

  @Override
  public int hashCode() {
    return ((Integer) this.getUserId()).hashCode();
  }

  /**
   * Return true if the user is the default admin
   *
   * @return true if the user is the default admin
   */
  public boolean isDefaultAdmin() {
    for (Role r : roles) {
      if (r.getRoleType().equals(RoleType.SUPER_ADMIN.toString())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return true if the user is an admin
   *
   * @return true if the user is an admin
   */
  public boolean isAdmin() {
    for (Role r : roles) {
      if (r.getRoleType().equals(RoleType.ADMIN.toString())
          || r.getRoleType().equals(RoleType.SUPER_ADMIN.toString())) {
        return true;
      }
    }
    return false;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public void setDeletedExpiry(Timestamp deletedExpiry) {
    this.deletedExpiry = deletedExpiry;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public List<Nationality> getNationalities() {
    return nationalities;
  }

  public void setNationalities(List<Nationality> nationalities) {
    this.nationalities = nationalities;
  }

  public Date getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(Date dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public List<TravellerType> getTravellerTypes() {
    return travellerTypes;
  }

  public void setTravellerTypes(List<TravellerType> travellerTypes) {
    this.travellerTypes = travellerTypes;
  }

  public List<Passport> getPassports() {
    return passports;
  }

  public void setPassports(List<Passport> passports) {
    this.passports = passports;
  }

  public Timestamp getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Timestamp timestamp) {
    this.timestamp = timestamp;
  }

  public List<PersonalPhoto> getPersonalPhotos() {
    return personalPhotos;
  }

  public void setPersonalPhotos(List<PersonalPhoto> personalPhotos) {
    this.personalPhotos = personalPhotos;
  }

  public PersonalPhoto getProfilePhoto() {
    return profilePhoto;
  }

  public void setProfilePhoto(PersonalPhoto profilePhoto) {
    this.profilePhoto = profilePhoto;
  }

  public PersonalPhoto getCoverPhoto() {
    return coverPhoto;
  }

  public void setCoverPhoto(PersonalPhoto coverPhoto) {
    this.coverPhoto = coverPhoto;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public List<Role> getRoles() {
    return roles;
  }

  public void setRoles(List<Role> roles) {
    this.roles = roles;
  }
}
