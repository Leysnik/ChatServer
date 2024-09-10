# ChatServer (Java Application)
This is the simple ChatServer app with message filtration and private messaging (user to user)
# About
This project can filter messages of users: you can add forbidden words in the current file, \
If someone typed too much bad words (count of words you can  change in the server main class), he will be removed from the chat. \
Also top of the user app contains JComboBox with all nicknames of connected users: this allows to send private messages (user to user)
# Setup
1. Import this repo
2. In ```ClientWindow.java``` change ```SERVER_ADDRESS``` to server current address (to use in local network just copy address from cmd ```ipconfig```)
3. In the root directory of the server app or project you must create file ```blacklist.txt``` where you can write forbidden word
4. start server, then start th;e main window and use