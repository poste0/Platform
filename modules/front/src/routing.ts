import VideoCards from "./app/video/VideoCards";
import { CameraManagement } from "./app/camera/CameraManagement";
import { getMenuItems } from "@cuba-platform/react";

export const menuItems = getMenuItems();

// Code below demonstrates how we can create SubMenu section
// Remove '/*' '*/' comments and restart app to get this block in menu

/*
// This is RouteItem object that we want to see in User Settings sub menu
const backToHomeRouteItem = {
  pathPattern: "/backToHome",
  menuLink: "/",
  component: null,
  caption: "home"
};
// SubMenu object
const userSettingsSubMenu = {
  caption: 'UserSettings', // add router.UserSettings key to src/i18n/en.json for valid caption
  items: [backToHomeRouteItem]};
// Add sub menu item to menu config
menuItems.push(userSettingsSubMenu);
*/

menuItems.push({
  pathPattern: "/cameraManagement/:entityId?",
  menuLink: "/cameraManagement",
  component: CameraManagement,
  caption: "CameraManagement"
});

menuItems.push({
  pathPattern: "/videoCards",
  menuLink: "/videoCards",
  component: VideoCards,
  caption: "VideoCards"
});