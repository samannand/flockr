import superagent from "superagent";
import { endpoint } from "./endpoint";
import UserStore from "../stores/UserStore";

/**
 * Router onEnter hook to check if a user is logged in, if user
 * is not logged in, redirect, otherwise go to page
 * @param {*} to The route to go to
 * @param {*} from The route the user is currently in
 * @param {*} next Functiont to change where the user is going
 */
export async function loggedIn(to, from, next) {
  const userId = localStorage.getItem("userId");
  const userToken = localStorage.getItem("authToken");

  if (!userId || !userToken) {
    next("/login");
    return;
  }

  let res;
  try {
    res = await  superagent.get(endpoint(`/travellers/${userId}`))
    .set("Authorization", userToken)
  } catch (e) {
    next("/login");    
    return;
  }

  if (!UserStore.methods.loggedIn()) {
    console.log("I am setting the data");
    UserStore.methods.setData(res.body);
  }
  next();
}