OpenKWSolver
============

Light and easy to use mobile [9kw.eu](http://www.9kw.eu/)-Solver. (~970kb!)

Using
-----

To work with it, you'll new an account on [9kw.eu](http://www.9kw.eu/), as well as an API-Key.
The app won't work without API-Key. You can get the current Version beta.1 apk [here](https://github.com/dotWee/OpenKWSolver/releases/download/beta.1/app-release.apk).

Building
--------

To build the app, just import OpenKWSolver into Android Studio.

Used Permissions
----------------

+ .INTERNET to request Captcha and download Image
+ .VIBRATE to signalize a new Captcha arrived

Features
--------

+ Vibrate as soon as new Captcha arrives
+ Auto-pull new Captcha
+ Captcha-debug mode
+ Self-only Captchas

Screenshots
-----------

<table sytle="border: 0px;">
<tr>
<td><img width="200px" src="Screenshot1.png" /></td>
</tr>
</table>

Todo
----

+ Use 9kw's history API to view answered Captchas
+ Make it look beautiful and tidy up the code
+ Servercheck in TextView on bottom
+ Backport to lower Android API's
+ Let user decide the loop mode
+ Add Checkboxes to menu
+ Log for better debug
+ Add Click mode
+ Documentation