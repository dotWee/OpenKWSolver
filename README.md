OpenKWSolver
============

Light and easy to use mobile [9kw.eu](http://www.9kw.eu/)-Solver. (~970kb!)

Using
-----

minSDK: 21 (5.0 Lollipop)
To work with it, you'll need a device with Android 5.0 and above. Also, an account on [9kw.eu](http://www.9kw.eu/), as well as an API-Key would be an advantage.
The app won't work without API-Key. 
You can grab the latest Version beta.5 apk [here](https://github.com/dotWee/OpenKWSolver/releases/download/beta.5/app-release_SIGNED_beta.5.apk).
md5sum: <code>b88473a9fe5b88782938a86073325573</code

Building
--------

Just clone that git-project, or download it as zip [here](https://github.com/dotWee/OpenKWSolver/archive/master-rewrite.zip) and import it to Android Studio!

Used Permissions
----------------

+ .ACCESS_NETWORK_STATE to check if network is available
+ .INTERNET to request Captcha and download Image
+ .VIBRATE to signalize a new Captcha arrived

Features
--------

+ Servercheck (Current worker-count, Captchas in Queue)
+ Vibrate on Captcha-arrival
+ Auto-pull new Captcha
+ Captcha-debug mode
+ Self-only Captchas

Screenshots
-----------

<table sytle="border: 0px;">
<tr>
<td><img width="200px" src="Screenshot.png" /></td>
</tr>
</table>

Todo
----

+ Use 9kw's history API to view answered Captchas
+ Make it look beautiful and tidy up the code
+ Let user decide the loop mode (in work)
+ Backport to lower Android API's
+ Add Click mode
+ Documentation
+ Add an Icon

Found a bug / had a force-close?
--------------------------------

Open a new Issue and provide a logcat.
