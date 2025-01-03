# Final Project for C323: Grocery List App
## Purpose: A grocery list sharing app that allows multiple users to access a shared list of grocery items via a unique token.
## Author: Timothy Gilmore


### App Screenshot:

![App Screenshot](https://github.com/gilmore-ec/323-Grocery-List/blob/development/assets/app_screenshot.PNG?raw=true)

### Requirements:
- #### User Experience:
   - [x] **Create a List**: Users can create a new grocery list and receive a randomly generated token. This token can be shared with others to allow them to join the list.
   - [x] **Join a List**: Users can enter a token to join an existing list. They will be asked to provide a nickname for identification purposes.
   - [x] All Firebase transactions should account for potential failures, such as connectivity issues or unsuccessful save/load operations. Errors should be handled gracefully, and appropriate **toast messages** should inform the user about the transaction status.
   - [x] The app should include an attractive **launcher icon** and use images or icons that clearly represent its functionalities.
- #### Navigation:
   - After joining a list, all screens should include an **app bar** with a two-item menu:
      - [x] **Theme**: Allows users to choose from three predefined themes. The selected theme should be applied to the entire app.
      - [x] **Help**: Opens a WebView with the link to the Google Support page: [](https://support.google.com/android/#topic=7313011).
   - Once a user joins a list, a **navigation fragment** with two items should be included on all screens:
      - [x] **Home**: Returns to the list’s home screen.
      - [x] **Logout**: Allows the user to leave the list.
   - Home Screen:
      - [x] The list home screen should show the **total cost** of all items at the bottom of the screen.
      - [x] Newly added items should **appear at the top** of the list.
- #### List:
   - [x] **Creating a List**: When creating a list, the user must first assign a **name** to the list before generating the token.
   - [x] **List Persistence**: After joining a list, the grocery list should be the **default screen** the user sees on every new app launch until they log out.
   - [x] A **RecyclerView** should be used to display the grocery list items with their details.
   - List Items:
      - [x] **Item Name**
      - [x] **Quantity** (as a string)
      - [x] **Buy Before Date** (using a date picker)
      - [x] **Price**
- #### Ownership and Edits:
   - [x] Each list item should be associated with the **user who added** it or the **last user to edit** it.
   - [x] Any user who has joined the list should be able to **edit or remove** any list item.
- #### Data Persistence:
   - [x] **Rotation of the device** should not cause data loss or errors, especially when entering or updating information.
- #### Other:
   - [x] Provide sufficient comments to explain your Kotlin code.
   - [x] Add the instructor as a contributor to your project, then add the TAs.
   - [x] Add a screenshot of the app to your repo. Show the screenshot in the README file.

### References:
![Proposed App Wireframe](https://github.com/gilmore-ec/323-Grocery-List/blob/development/assets/Final_Project_Wireframe.PNG?raw=true)
