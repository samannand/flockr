<template>
  <v-dialog v-model="this.showForm" persistent max-width="600px">
    <v-card>
      <v-card-title>
        <span class="headline">Editing user: {{this.fullUserName}}</span>
      </v-card-title>
      <v-card-text>
        <v-form ref="form">
          <v-container grid-list-md>
            <v-layout wrap>
              <v-flex xs12 sm6 md4>
                <v-text-field
                        label="First name *"
                        required
                        :value="this.initialUserData.firstName"
                        v-on:input="addChange('firstName', $event)"
                        :rules="[requiredRule, lengthRule]"
                />
              </v-flex>
              <v-flex xs12 sm6 md4>
                <v-text-field
                        label="Middle name"
                        :value="this.initialUserData.middleName"
                        v-on:input="addChange('middleName', $event)"
                        hint="you may leave this empty"
                        :rules="[middleNameRule]"
                />
              </v-flex>
              <v-flex xs12 sm6 md4>
                <v-text-field
                        label="Last name *"
                        :value="this.initialUserData.lastName"
                        v-on:input="addChange('lastName', $event)"
                        required
                        :rules="[requiredRule, lengthRule]"
                />
              </v-flex>
              <v-flex xs12>
                <v-text-field
                        label="Email *"
                        :value="this.initialUserData.email"
                        v-on:input="addChange('email', $event)"
                        required
                        :rules="[requiredRule, emailRule]"
                />
              </v-flex>
              <v-flex xs12 sm6>
                <v-autocomplete
                        :items="allTravellerTypeNames"
                        :value="this.initialUserTravellerTypeNames"
                        label="Traveller types"
                        v-on:input="addChange('travellerTypes', parseEditTravellerTypeChangeEvent($event))"
                        multiple
                ></v-autocomplete>
              </v-flex>
              <v-flex xs12 sm6>
                <v-autocomplete
                        :items="allUserRoleTypes"
                        :value="initialUserRoleTypes"
                        label="User roles *"
                        v-on:input="addChange('roles', $event)"
                        multiple
                        :rules="[requiredRule]"
                ></v-autocomplete>
              </v-flex>
              <v-flex xs12 sm6>
                <v-autocomplete
                        :items="allPassportCountries"
                        :value="initialUserPassportCountries"
                        label="Passports"
                        v-on:input="addChange('passports', parseEditPassportsChangeEvent($event))"
                        multiple
                ></v-autocomplete>
              </v-flex>
              <v-flex xs12 sm6>
                <v-autocomplete
                        :items="allNationalityNames"
                        :value="initialUserNationalityNames"
                        label="Nationalities *"
                        v-on:input="addChange('nationalities', parseEditNationalityChangeEvent($event))"
                        :rules="[arrayRequiredRule]"
                        multiple
                ></v-autocomplete>
              </v-flex>
              <v-flex xs12 sm6>
                <v-autocomplete
                        :items="['Female', 'Male', 'Other']"
                        label="Gender *"
                        :value="initialUserData.gender"
                        v-on:input="addChange('gender', $event)"
                        :rules="[requiredRule]"
                ></v-autocomplete>
              </v-flex>
              <v-flex xs12 sm6>
                <v-menu ref="dateMenu" v-model="dateMenu" :close-on-content-click="false" :nudge-right="40"
                        :return-value.sync="dateOfBirth" lazy transition="scale-transition" offset-y full-width>
                  <template v-slot:activator="{ on }">
                    <v-text-field class="edit-field" label="Date of Birth" v-model="dateOfBirth" readonly v-on="on">

                    </v-text-field>
                  </template>
                  <v-date-picker color="secondary" ref="picker" :max="currentDate" v-model="dateOfBirth" no-title
                                 scrollable @change="saveDate">
                    <v-spacer></v-spacer>
                    <v-btn flat color="primary" @click="dateMenu = false">Cancel</v-btn>
                    <v-btn flat color="primary" @click="$refs.dateMenu.save(dateOfBirth)">OK</v-btn>
                  </v-date-picker>
                </v-menu>
              </v-flex>
            </v-layout>
          </v-container>
        </v-form>
        <small>* indicates required field</small>
      </v-card-text>
      <v-card-actions>
        <v-spacer/>
        <v-btn color="blue darken-1" flat @click="dismissForm">Cancel</v-btn>
        <v-btn color="blue darken-1" flat @click="submitForm">Save</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
  import superagent from 'superagent';
  import {endpoint} from '../../../utils/endpoint';
  import moment from 'moment';

  export default {
    props: {
      initialUserData: {
        "userId": Number,
        "firstName": String,
        "middleName": String,
        "lastName": String,
        "dateOfBirth": String,
        "email": String,
        "gender": String,
        "nationalities": [
          {
            "nationalityId": Number,
            "nationalityName": String
          }
        ],
        "passports": [
          {
            "passportId": Number,
            "passportCountry": String
          }
        ],
        "travellerTypes": [
          {
            "travellerTypeId": Number,
            "travellerTypeName": String
          }
        ],
        "roles": [
          {
            "roleId": Number,
            "roleType": String
          }
        ],
        "timestamp": Number,
      },
      showForm: {
        type: Boolean,
        required: true
      }
    },
    data() {
      return {
        changes: {
          // contains changes to be pushed to make a patch on the user
        },
        allNationalities: [],
        allPassports: [],
        allTravellerTypes: [],
        allUserRoles: [],
        dateMenu: false, // don't show the menu by default
        dateOfBirth: null,
        currentDate: moment().format("YYYY-MM-DD"),
        arrayRequiredRule: field => field.length > 0 || "At least one is required",
        requiredRule: field => !!field || "This field is required",
        lengthRule: field => field.length >= 2 || "This field must be 2 or more characters long",
        emailRule: field => /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/.test(field) || "This is not a valid email"
      }
    },
    methods: {
      /**
       * Shows an error snackbar
       */
      showErrorSnackbar(text) {
        this.$root.$emit("show-snackbar", {
          message: text,
          color: "error",
          timeout: 5000
        });
      },
      /**
       * Stop the form from displaying by emitting call to parent.
       */
      dismissForm: function () {
        this.$emit('dismissForm');
      },
      /**
       * Check the form is valid and then emit a call to submit the form.
       * Emit a call to show the form contains invalid data if invalid.
       */
      submitForm: function () {
        if (this.$refs.form.validate()) {
          this.$emit('submitForm', this.changes);
        } else {
          this.$emit('incorrectData');
        }
      },
      // store the patches we want to make to the user for when the form is submitted
      // NOTE: if you are unsure about how this works, open up the component in Vue Dev Tools and look
      // at the object where changes are stored as you make changes to the fields
      addChange: function (fieldKey, fieldValue) {
        this.changes.userId = this.initialUserData.userId; // make sure there's always a user id
        if (fieldKey === 'nationalities') {
          // if trying to make a user have no nationalities, do not add the change
          if (fieldValue.length < 1) {
            return;
          }
        }
        this.changes[fieldKey] = fieldValue; // store the change
      },
      /**
       * Gets all the nationalities
       */
      getAllNationalities: async function () {
        try {
          const res = await superagent.get(endpoint('/users/nationalities'));
          this.allNationalities = res.body;
        } catch (err) {
          this.showErrorSnackbar("Could not get all valid nationalities.");
        }
      },
      /**
       * Gets all the passports
       */
      getAllPassports: async function () {
        try {
          const res = await superagent.get(endpoint('/users/passports'));
          this.allPassports = res.body;
        } catch (err) {
          this.showErrorSnackbar("Could not get all valid passports.");
        }
      },
      /**
       * Gets all the traveller types
       */
      getAllTravellerTypes: async function () {
        try {
          const res = await superagent.get(endpoint('/users/types')).set("Authorization", localStorage.getItem("authToken"));
          this.allTravellerTypes = res.body;
        } catch (err) {
          this.showErrorSnackbar("Could not get all valid traveller types.");
        }
      },
      /**
       * Gets all the user roles
       */
      getAllUserRoles: async function () {
        try {
          const res = await superagent.get(endpoint('/users/roles')).set("Authorization", localStorage.getItem("authToken"));
          this.allUserRoles = res.body.filter(role => role.roleType !== "SUPER_ADMIN");
        } catch (err) {
          this.showErrorSnackbar("Could not get all user roles.");
        }
      },
      /**
       * Transform an array of traveller type names into their corresponding ids.
       * @param travellerTypeNames array of traveller type names.
       * @return array of traveller type ids.
       */
      parseEditTravellerTypeChangeEvent: function (travellerTypeNames) {
        // filter all the traveller types to be only the ones with the name included in the event, then map them to their ids
        return this.allTravellerTypes.filter((travellerType) => travellerTypeNames.includes(travellerType.travellerTypeName))
            .map((travellerType) => travellerType.travellerTypeId);
      },
      /**
       * Transform an array of passport names into their corresponding ids.
       * @param passportNames array of passport names.
       * @returns {(NumberConstructor | *)[]} array of passport ids.
       */
      parseEditPassportsChangeEvent: function (passportNames) {
        // filter all the traveller types to be only the ones with the name included in the event,
        // then map them to their ids
        return this.allPassports.filter((passport) => passportNames.includes(passport.passportCountry))
            .map((passport) => passport.passportId);
      },
      /**
       * Transform an array of nationality names into their corresponding ids.
       * @param nationaltyNames array of nationality names.
       * @returns {(NumberConstructor | *)[]} array of nationality ids.
       */
      parseEditNationalityChangeEvent: function (nationaltyNames) {
        return this.allNationalities.filter((nationality) => nationaltyNames.includes(nationality.nationalityName))
            .map((nationality) => nationality.nationalityId);
      },
      /**
       * Saves the date
       * @param date the new date to be saved
       */
      saveDate(date) {
        this.$refs.dateMenu.save(date);
      }
    },
    computed: {
      /**
       * Formats the name of the user to be first name followed by middle name then last name
       */
      fullUserName: function () {
        return `${this.initialUserData.firstName} ${this.initialUserData.middleName ? this.initialUserData.middleName : ''} ${this.initialUserData.lastName}`;
      },
      /**
       * Formats the user's traveller type list to be traveller type names
       */
      initialUserTravellerTypeNames: function () {
        return this.initialUserData.travellerTypes.map((travellerType) => travellerType.travellerTypeName);
      },
      /**
       * Formats the user's nationality list to be nationality names
       */
      initialUserNationalityNames: function () {
        return this.initialUserData.nationalities.map((nationality) => nationality.nationalityName);
      },
      /**
       * Formats the user's passport countries list to be country names
       */
      initialUserPassportCountries: function () {
        return this.initialUserData.passports.map((passport) => passport.passportCountry);
      },
      /**
       * Formats the user's role list to be role names
       */
      initialUserRoleTypes: function () {
        return this.initialUserData.roles.map((role) => role.roleType);
      },
      /**
       * Formats all the nationality list to be nationality names
       */
      allNationalityNames: function () {
        return this.allNationalities.map((nationality) => nationality.nationalityName);
      },
      /**
       * Formats all the passport countries list to be country names
       */
      allPassportCountries: function () {
        return this.allPassports.map((passport) => passport.passportCountry);
      },
      /**
       * Formats all the role list to be role names
       */
      allUserRoleTypes: function () {
        return this.allUserRoles.map((role) => role.roleType);
      },
      /**
       * Formats all the traveller type list to be traveller type names
       */
      allTravellerTypeNames: function () {
        return this.allTravellerTypes.map((travellerType) => travellerType.travellerTypeName);
      }
    },
    mounted() {
      this.dateOfBirth = moment(this.initialUserData.dateOfBirth).format("YYYY-MM-DD");
      this.getAllUserRoles();
      this.getAllNationalities();
      this.getAllPassports();
      this.getAllTravellerTypes();
    },
    watch: {
      dateMenu(date) {
        date && setTimeout(() => (this.$refs.picker.activePicker = "YEAR"));
      },
      /**
       * Changes the date of birth to the new DoB value chosen by the user
       * @param val
       */
      dateOfBirth(val) {
        this.addChange('dateOfBirth', val);
      }
    }
  }
</script>

