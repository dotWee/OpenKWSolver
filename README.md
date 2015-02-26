OpenKWSolver
============

Light and easy to use mobile [9kw.eu](http://www.9kw.eu/)-Solver. (~970kb!)

Using
-----

minSDK: 16 (4.0 Ice Cream Sandwich)
targetSDK: 21 (5.0 Lollipop)

To use OpenKWSolver, you need an account on [9kw.eu](http://www.9kw.eu/), as well as an API-Key (grab one at [9kw.eu/userapi](http://www.9kw.eu/userapi.html)).
Latest release (beta.7) is available [here](https://github.com/dotWee/OpenKWSolver/blob/master/app-release.apk?raw=true) or as debug-version [here](https://github.com/dotWee/OpenKWSolver/blob/master/app-debug.apk?raw=true).
md5sum (release): <code>dbe010a625bf135e8064a052f66d5f62</code>
md5sum (debug): <code>d60df8718fbc798c3ac5a1b0a8234586</code>

Building
--------

Just clone that git-project, or download it as zip [here](https://github.com/dotwee/OpenKWSolver/archive/master.zip), extract and import it into Android Studio!

Used Permissions
----------------

+ .ACCESS_NETWORK_STATE to check if network is available
+ .INTERNET to request Captcha and download Image
+ .VIBRATE to signalize a new Captcha arrived

Features
--------

+ View current quantity of workers and Captchas in queue
+ Vibrate on Captcha-arrival
+ Auto-pull new Captcha
+ Captcha-debug mode
+ Self-only Captchas

Screenshots
-----------

<table style="border: 0px;">
<tr>
<td><img width="200px" src="Screenshot.png" /></td>
</tr>
</table>

Todo
----

+ Let user decide the loop mode (currently in work)
+ Use 9kw's history API to view answered Captchas
+ Make it look beautiful and tidy up the code
+ Add Click mode
+ Documentation
+ Add an Icon

Found a bug / had a force-close?
--------------------------------

Open a new Issue and provide a logcat.

License
-------

Copyright 2015 Lukas Wolfsteiner

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.