package com.muziko.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.MiniPlayer;
import com.muziko.events.BufferingEvent;
import com.muziko.manager.AppController;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hugo.weaving.DebugLog;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends BaseActivity {

	private CoordinatorLayout coordinatorlayout;
	private Toolbar toolbar;
	private AppBarLayout appBarLayout;
	private RelativeLayout content;
	private SlidingUpPanelLayout slidingUpPanelLayout;
	private MiniPlayer miniPlayer;
	private MainReceiver mainReceiver;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(false);
        setContentView(R.layout.activity_about);
		findViewsById();

		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle(getString(R.string.about));

		View aboutPage = new AboutPage(this)
				.isRTL(false)
				.setImage(R.drawable.splash_logo)
				.setDescription(getString(R.string.about_page_desc))
				.addItem(new Element().setTitle("Version " + MyApplication.versionName))
				.addItem(getPrivacyElement())
				.addItem(getLicenseElement())
				.addGroup("Connect with us")
				.addEmail("muzikoapp@gmail.com")
				.addFacebook("muzikoplayer")
				.addPlayStore("com.muziko")
				.create();

		content.addView(aboutPage);

		setupMainPlayer();

		EventBus.getDefault().register(this);
	}

    @Override
    public void onDestroy() {
        if (miniPlayer != null) miniPlayer.onDestroy();
        unregister();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (miniPlayer != null) {
            miniPlayer.pause();
        }
        unregister();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        register();
        mainUpdate();
    }

	private void findViewsById() {
		coordinatorlayout = findViewById(R.id.coordinatorlayout);
		appBarLayout = findViewById(R.id.appBarLayout);
		toolbar = findViewById(R.id.toolbar);
		content = findViewById(R.id.contentLayout);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button

			case android.R.id.home:
				onBackPressed();
				return true;

		}
		return super.onOptionsItemSelected(item);
	}

	private Element getPrivacyElement() {
		Element privacyElement = new Element();
		privacyElement.setTitle("Privacy");
//		copyRightsElement.setIconDrawable(R.drawable.about_icon_copy_right);
//		copyRightsElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color);
//		copyRightsElement.setIconNightTint(android.R.color.white);
//		copyRightsElement.setGravity(Gravity.CENTER);
		privacyElement.setOnClickListener(v -> new MaterialDialog.Builder(AboutActivity.this)
				.title(R.string.privacy_policy_title)
				.content("What personal information do we collect from the people that visit our blog, website or app?\n" +
						"\n" +
						"When purchasing any in-app product or registering on our site, as appropriate, you may be asked to enter your name, email address, phone number or other details to help you with your experience.\n" +
						"\n" +
						"When do we collect information?\n" +
						"\n" +
						"We collect information from you when you register on our site or enter information on our site.\n" +
						"\n" +
						"How do we use your information?\n" +
						"\n" +
						"We may use the information we collect from you when you register, make a purchase, Share songs with your contacts or use certain other site features in the following ways:\n" +
						"\n" +
						"To personalize your experience and to allow us to deliver the type of content and product offerings in which you are most interested.\n" +
						"\n" +
						"How do we protect your information?\n" +
						"\n" +
						"We do not share or keep any of your information saved with us. Our Firebase is scanned on a regular basis for security holes and known vulnerabilities in order to make your visit to our App as safe as possible.\n" +
						"\n" +
						"\n" +
						"Third-party disclosure\n" +
						"\n" +
						"We do not sell, trade, or otherwise transfer to outside parties your Personally Identifiable Information.\n" +
						"\n" +
						"Third-party links\n" +
						"\n" +
						"We do not include or offer third-party products or services on our App.\n" +
						"\n" +
						"Ad Monetization\n" +
						"\n" +
						"We have enabled Google ad monetization on our app but not for the users who are premium or have purchased our ad-free version.\n" +
						"\n" +
						"We may change our privacy policy in near future and you will be notified of any Privacy Policy changes:\n" +
						"      • On our Privacy Policy Page\n" +
						"\n" +
						"You can change your personal information:\n" +
						"      • By emailing us\n" +
						"\n" +
						"Contacting Us\n" +
						"\n" +
						"If there are any questions regarding this privacy policy, you may contact us using the information below.\n" +
						"\n" +
						"muzikoapp@gmail.com\n" +
						"\n" +
						"Last Edited on 20-02-2017")
				.positiveText(R.string.alert_ok_button)
				.show());
		return privacyElement;
	}

	private Element getLicenseElement() {
		Element privacyElement = new Element();
//		final String copyrights = String.format(getString(R.string.copy_right), Calendar.getInstance().get(Calendar.YEAR));
		privacyElement.setTitle("Licenses");
//		copyRightsElement.setIconDrawable(R.drawable.about_icon_copy_right);
//		copyRightsElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color);
//		copyRightsElement.setIconNightTint(android.R.color.white);
//		copyRightsElement.setGravity(Gravity.CENTER);
		privacyElement.setOnClickListener(v -> new MaterialDialog.Builder(AboutActivity.this)
				.title(R.string.licenses)
				.content("Android Support Libraries\n" +
						"\n" +
						"\n" +
						"Firebase\n" +
						"\n" +
						"\n" +
						"AndroidSlidingUpPanel\n" +
						"\n" +
						"https://github.com/umano/AndroidSlidingUpPanel\n" +
						"\n" +
						"Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:\n" +
						"\n" +
						"http://www.apache.org/licenses/LICENSE-2.0\n" +
						"\n" +
						"Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.\n" +
						"\n" +
						"\n" +
						"Android-Image-Cropper\n" +
						"\n" +
						"https://github.com/ArthurHub/Android-Image-Cropper\n" +
						"\n" +
						"Originally forked from edmodo/cropper.\n" +
						"\n" +
						"Copyright 2016, Arthur Teplitzki, 2013, Edmodo, Inc.\n" +
						"\n" +
						"Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:\n" +
						"\n" +
						"http://www.apache.org/licenses/LICENSE-2.0\n" +
						"\n" +
						"Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.\n" +
						"\n" +
						"\n" +
						"Picasso\n" +
						"\n" +
						"http://square.github.io/picasso/\n" +
						"\n" +
						"Copyright 2013 Square, Inc.\n" +
						"\n" +
						"Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
						"you may not use this file except in compliance with the License.\n" +
						"You may obtain a copy of the License at\n" +
						"\n" +
						"   http://www.apache.org/licenses/LICENSE-2.0\n" +
						"\n" +
						"Unless required by applicable law or agreed to in writing, software\n" +
						"distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
						"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
						"See the License for the specific language governing permissions and\n" +
						"limitations under the License.\n" +
						"\n" +
						"\n" +
						"CircleImageView\n" +
						"\n" +
						"https://github.com/hdodenhof/CircleImageView\n" +
						"\n" +
						"Copyright 2014 - 2017 Henning Dodenhof\n" +
						"\n" +
						"Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
						"you may not use this file except in compliance with the License.\n" +
						"You may obtain a copy of the License at\n" +
						"\n" +
						"    http://www.apache.org/licenses/LICENSE-2.0\n" +
						"\n" +
						"Unless required by applicable law or agreed to in writing, software\n" +
						"distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
						"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
						"See the License for the specific language governing permissions and\n" +
						"limitations under the License.\n" +
						"\n" +
						"\n" +
						"FloatingActionButton\n" +
						"\n" +
						"https://github.com/Clans/FloatingActionButton\n" +
						"\n" +
						"Copyright 2015 Dmytro Tarianyk\n" +
						"\n" +
						"Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
						"you may not use this file except in compliance with the License.\n" +
						"You may obtain a copy of the License at\n" +
						"\n" +
						"   http://www.apache.org/licenses/LICENSE-2.0\n" +
						"\n" +
						"Unless required by applicable law or agreed to in writing, software\n" +
						"distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
						"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
						"See the License for the specific language governing permissions and\n" +
						"limitations under the License.\n" +
						"\n" +
						"\n" +
						"AppCompat-Extension-Library\n" +
						"\n" +
						"https://github.com/TR4Android/AppCompat-Extension-Library\n" +
						"\n" +
						"Copyright 2015 Thomas Robert Altstidl & fountaingeyser\n" +
						"\n" +
						"Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at\n" +
						"\n" +
						"http://www.apache.org/licenses/LICENSE-2.0\n" +
						"Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.\n" +
						"\n" +
						"\n" +
						"ProperRatingBar\n" +
						"\n" +
						"https://github.com/techery/ProperRatingBar\n" +
						"\n" +
						"The MIT License (MIT)\n" +
						"\n" +
						"Copyright (c) 2016 Techery (http://techery.io/)\n" +
						"\n" +
						"Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
						"of this software and associated documentation files (the \"Software\"), to deal\n" +
						"in the Software without restriction, including without limitation the rights\n" +
						"to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
						"copies of the Software, and to permit persons to whom the Software is\n" +
						"furnished to do so, subject to the following conditions:\n" +
						"\n" +
						"The above copyright notice and this permission notice shall be included in\n" +
						"all copies or substantial portions of the Software.\n" +
						"\n" +
						"THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
						"IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
						"FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
						"AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
						"LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
						"OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN\n" +
						"THE SOFTWARE.\n" +
						"\n" +
						"\n" +
						"WheelPicker\n" +
						"\n" +
						"https://github.com/AigeStudio/WheelPicker\n" +
						"\n" +
						"Copyright 2015-2017 AigeStudio\n" +
						"\n" +
						"Licensed under the Apache License, Version 2.0 (the \"License\");you may not use this file except in compliance with the License.\n" +
						"\n" +
						"You may obtain a copy of the License at\n" +
						"\n" +
						"http://www.apache.org/licenses/LICENSE-2.0\n" +
						"\n" +
						"Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.\n" +
						"\n" +
						"\n" +
						"MaterialLoadingProgressBar\n" +
						"\n" +
						"https://github.com/lsjwzh/MaterialLoadingProgressBar\n" +
						"\n" +
						"Copyright 2014 lsjwzh\n" +
						"\n" +
						"Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
						"you may not use this file except in compliance with the License.\n" +
						"You may obtain a copy of the License at\n" +
						"\n" +
						"    http://www.apache.org/licenses/LICENSE-2.0\n" +
						"\n" +
						"Unless required by applicable law or agreed to in writing, software\n" +
						"distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
						"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
						"See the License for the specific language governing permissions and\n" +
						"limitations under the License.\n" +
						"\n" +
						"\n" +
						"SuperToasts\n" +
						"\n" +
						"https://github.com/JohnPersano/SuperToasts\n" +
						"\n" +
						"Copyright 2013-2016 John Persano\n" +
						"\n" +
						"Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
						"you may not use this file except in compliance with the License.\n" +
						"You may obtain a copy of the License at\n" +
						"\n" +
						"   http://www.apache.org/licenses/LICENSE-2.0\n" +
						"\n" +
						"Unless required by applicable law or agreed to in writing, software\n" +
						"distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
						"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
						"See the License for the specific language governing permissions and\n" +
						"limitations under the License.\n" +
						"\n" +
						"\n" +
						"material-dialogs\n" +
						"\n" +
						"https://github.com/afollestad/material-dialogs\n" +
						"\n" +
						"The MIT License (MIT)\n" +
						"\n" +
						"Copyright (c) 2014-2016 Aidan Michael Follestad\n" +
						"\n" +
						"Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
						"of this software and associated documentation files (the \"Software\"), to deal\n" +
						"in the Software without restriction, including without limitation the rights\n" +
						"to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
						"copies of the Software, and to permit persons to whom the Software is\n" +
						"furnished to do so, subject to the following conditions:\n" +
						"\n" +
						"The above copyright notice and this permission notice shall be included in all\n" +
						"copies or substantial portions of the Software.\n" +
						"\n" +
						"THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
						"IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
						"FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
						"AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
						"LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
						"OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n" +
						"SOFTWARE.\n" +
						"\n" +
						"\n" +
						"bottomsheet\n" +
						"\n" +
						"https://github.com/Flipboard/bottomsheet\n" +
						"\n" +
						"Copyright (c) 2015, Flipboard\n" +
						"All rights reserved.\n" +
						"\n" +
						"Redistribution and use in source and binary forms, with or without modification,\n" +
						"are permitted provided that the following conditions are met:\n" +
						"\n" +
						"* Redistributions of source code must retain the above copyright notice, this\n" +
						"  list of conditions and the following disclaimer.\n" +
						"\n" +
						"* Redistributions in binary form must reproduce the above copyright notice, this\n" +
						"  list of conditions and the following disclaimer in the documentation and/or\n" +
						"  other materials provided with the distribution.\n" +
						"\n" +
						"* Neither the name of Flipboard nor the names of its\n" +
						"  contributors may be used to endorse or promote products derived from\n" +
						"  this software without specific prior written permission.\n" +
						"\n" +
						"THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND\n" +
						"ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED\n" +
						"WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE\n" +
						"DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR\n" +
						"ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES\n" +
						"(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;\n" +
						"LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON\n" +
						"ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n" +
						"(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS\n" +
						"SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n" +
						"\n" +
						"\n" +
						"android-job\n" +
						"\n" +
						"https://github.com/evernote/android-job\n" +
						"\n" +
						"Copyright (c) 2007-2016 by Evernote Corporation, All rights reserved.\n" +
						"\n" +
						"Use of the source code and binary libraries included in this package\n" +
						"is permitted under the following terms:\n" +
						"\n" +
						"Redistribution and use in source and binary forms, with or without\n" +
						"modification, are permitted provided that the following conditions\n" +
						"are met:\n" +
						"\n" +
						"    1. Redistributions of source code must retain the above copyright\n" +
						"    notice, this list of conditions and the following disclaimer.\n" +
						"    2. Redistributions in binary form must reproduce the above copyright\n" +
						"    notice, this list of conditions and the following disclaimer in the\n" +
						"    documentation and/or other materials provided with the distribution.\n" +
						"\n" +
						"THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\n" +
						"IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\n" +
						"OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\n" +
						"IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\n" +
						"INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\n" +
						"NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\n" +
						"DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\n" +
						"THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n" +
						"(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF\n" +
						"THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n" +
						"\n" +
						"\n" +
						"AsyncJobLibrary\n" +
						"\n" +
						"https://github.com/Arasthel/AsyncJobLibrary\n" +
						"\n" +
						"Copyright 2014 Jorge Martín Espinosa (Arasthel)\n" +
						"\n" +
						"Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
						"you may not use this file except in compliance with the License.\n" +
						"You may obtain a copy of the License at\n" +
						"\n" +
						"    http://www.apache.org/licenses/LICENSE-2.0\n" +
						"\n" +
						"Unless required by applicable law or agreed to in writing, software\n" +
						"distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
						"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
						"See the License for the specific language governing permissions and\n" +
						"limitations under the License.\n" +
						"\n" +
						"\n" +
						"EventBus\n" +
						"\n" +
						"https://github.com/greenrobot/EventBus\n" +
						"\n" +
						"Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)\n" +
						"\n" +
						"EventBus binaries and source code can be used according to the Apache License, Version 2.0.\n" +
						"\n" +
						"Calligraphy\n" +
						"\n" +
						"https://github.com/chrisjenx/Calligraphy\n" +
						"\n" +
						"Copyright 2013 Christopher Jenkins\n" +
						"\n" +
						"Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
						"you may not use this file except in compliance with the License.\n" +
						"You may obtain a copy of the License at\n" +
						"\n" +
						"    http://www.apache.org/licenses/LICENSE-2.0\n" +
						"\n" +
						"Unless required by applicable law or agreed to in writing, software\n" +
						"distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
						"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
						"See the License for the specific language governing permissions and\n" +
						"limitations under the License.\n" +
						"\n" +
						"\n" +
						"retrofit\n" +
						"\n" +
						"https://square.github.io/retrofit/\n" +
						"\n" +
						"Copyright 2013 Square, Inc.\n" +
						"\n" +
						"Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
						"you may not use this file except in compliance with the License.\n" +
						"You may obtain a copy of the License at\n" +
						"\n" +
						"   http://www.apache.org/licenses/LICENSE-2.0\n" +
						"\n" +
						"Unless required by applicable law or agreed to in writing, software\n" +
						"distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
						"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
						"See the License for the specific language governing permissions and\n" +
						"limitations under the License.\n" +
						"\n" +
						"\n")
				.positiveText(R.string.alert_ok_button)
				.show());
		return privacyElement;
	}

	private void setupMainPlayer() {

		RelativeLayout contentlayout = findViewById(R.id.contentlayout);
		slidingUpPanelLayout = findViewById(R.id.sliding_layout);
		final LinearLayout mainPlayerLayout = findViewById(R.id.mainPlayerLayout);

		miniPlayer = new MiniPlayer(this, mainPlayerLayout, slidingUpPanelLayout, contentlayout);
	}

	private void mainUpdate() {
		miniPlayer.updateUI();
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkEvent(NetworkEvent networkEvent) {

        if (miniPlayer != null) {
            miniPlayer.updateUI();
        }
    }

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onProgressEvent(ProgressEvent event) {

        miniPlayer.updateProgress(event.getProgress(), event.getDuration());
    }

    @DebugLog
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBufferingEvent(BufferingEvent event) {

        if (miniPlayer != null) {
            miniPlayer.showBufferingMessage(event.getMessage(), event.isClose());
        }
    }

	private void register() {

		IntentFilter filter = new IntentFilter();
		filter.addAction(AppController.INTENT_EXIT);
		filter.addAction(AppController.INTENT_CLEAR);

		filter.addAction(AppController.INTENT_TRACK_EDITED);
		filter.addAction(AppController.INTENT_TRACK_SEEKED);
		filter.addAction(AppController.INTENT_QUEUE_STOPPED);
		filter.addAction(AppController.INTENT_TRACK_SHUFFLE);
		filter.addAction(AppController.INTENT_TRACK_REPEAT);
		filter.addAction(AppController.INTENT_QUEUE_CHANGED);
		filter.addAction(AppController.INTENT_QUEUE_CLEARED);

		mainReceiver = new MainReceiver();
		registerReceiver(mainReceiver, filter);
	}

	private void unregister() {

		if (mainReceiver != null) {
			unregisterReceiver(mainReceiver);
			mainReceiver = null;
		}
	}

	private class MainReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				String action = intent.getAction();

				switch (action) {
					case AppController.INTENT_TRACK_SEEKED:
						mainUpdate();
						break;
					case AppController.INTENT_QUEUE_CHANGED:
						mainUpdate();
						break;
					case AppController.INTENT_TRACK_REPEAT:
						mainUpdate();
						break;
					case AppController.INTENT_TRACK_SHUFFLE:
						mainUpdate();
						break;
					case AppController.INTENT_QUEUE_STOPPED:
                        miniPlayer.layoutMiniPlayer();

						break;
					case AppController.INTENT_QUEUE_CLEARED:
                        miniPlayer.layoutMiniPlayer();

						break;
					case AppController.INTENT_CLEAR:

						finish();
						break;
					case AppController.INTENT_EXIT:

						finish();
						break;
					case AppController.INTENT_TRACK_EDITED:
						int index = intent.getIntExtra("index", -1);
						String tag = intent.getStringExtra("tag");
						QueueItem item = (QueueItem) intent.getSerializableExtra("item");
						if (item != null) {
							mainUpdate();
						}
						break;
				}
			}
		}
	}
}
