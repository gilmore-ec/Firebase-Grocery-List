# Grocery List App
## Purpose: A grocery list sharing app that allows multiple users to access a shared list of grocery items via a unique token.
## Author: Timothy Gilmore

### Requirements:
- #### User Experience:
   - **Create a List**: Users can create a new grocery list and receive a randomly generated token. This token can be shared with others to allow them to join the list.
   - **Join a List**: Users can enter a token to join an existing list. They will be asked to provide a nickname for identification purposes.
   - All Firebase transactions account for potential failures, such as connectivity issues or unsuccessful save/load operations. Errors are be handled gracefully, and appropriate **toast messages** inform the user about the transaction status.
   - The app includes an attractive custom-designed **launcher icon** and uses images and icons that clearly represent its functionalities.
- #### Navigation:
   - After joining a list, all screens include an **app bar** with a two-item menu:
      - **Theme**: Allows users to choose from three predefined themes. The selected theme is applied to the entire app.
      - **Help**: Opens a WebView with the link to the Google Support page: [](https://support.google.com/android/#topic=7313011).
   - Once a user joins a list, a **navigation fragment** with two items is included on all screens:
      - **Home**: Returns to the list’s home screen.
      - **Logout**: Allows the user to leave the list.
   - Home Screen:
      - The list home screen shows the **total cost** of all items at the bottom of the screen.
      - Newly added items **appear at the top** of the list.
- #### List:
   - **Creating a List**: When creating a list, the user must first assign a **name** to the list before generating the token.
   - **List Persistence**: After joining a list, the grocery list will be the **default screen** the user sees on every new app launch until they log out.
   - The grocery list items with their details are displayed using a **RecyclerView**.
   - List Items contain the following data:
      - **Item Name**
      - **Quantity** (as a string)
      - **Buy Before Date** (using a date picker)
      - **Price**
- #### Ownership and Edits:
   - Each list item can be associated with the user who added it or the last user to edit it.
   - Any user who has joined the list is able to edit or remove any list item.
- #### Data Persistence:
   - Data is persistent and securely stored for the users in Firestore

### References:
![Application Wireframe](https://github.com/gilmore-ec/323-Grocery-List/blob/development/assets/Final_Project_Wireframe.PNG?raw=true)

### App Screenshot:

![App Screenshot](https://github.com/gilmore-ec/323-Grocery-List/blob/development/assets/app_screenshot.PNG?raw=true)
