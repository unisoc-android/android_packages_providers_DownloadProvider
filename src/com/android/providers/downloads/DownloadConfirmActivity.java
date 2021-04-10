/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.providers.downloads;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.util.Log;
import android.os.Bundle;
import android.app.KeyguardManager;
import static com.android.providers.downloads.Constants.TAG;

/*
** Download Alert dialog will be dismissed once the activity stops.
** And there is no special requirement for Dialog to attach to the activity.
** Otherwise DialogFragment should be used.
*/
public class DownloadConfirmActivity extends Activity {
    private static DownloadConfirmActivity sInstance;
    private static AlertDialog mDialog;
    private static int mConfirmToDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sInstance = this;
        mConfirmToDownload = 0;

        final AlertDialog.Builder builder=new AlertDialog.Builder(sInstance,
                AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setMessage(getString(R.string.download_confirm_msg));
        builder.setTitle(android.R.string.dialog_alert_title);
        builder.setIcon(getDrawable(R.mipmap.ic_dialog_alert_coloured));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mConfirmToDownload = 1;
                mDialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mConfirmToDownload = 0;
                mDialog.dismiss();
            }
        });
        mDialog = builder.create();
        mDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mDialog = null;
                onPause();
            }
        });
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        KeyguardManager km = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);;
        boolean isInKeyguard = km.isKeyguardLocked();
        if (isInKeyguard) {
            Log.d(TAG, "onPause  do nothing because of Keyguard!");
            return;
        }
        if (sInstance != null) {
            DealWithConfirmResult();
            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
            sInstance = null;
            if (!isFinishing()) {
                finish();
            }
        }
    }

    private void DealWithConfirmResult() {
        if (mConfirmToDownload == 0 || mConfirmToDownload == 1) {
            final Intent intent = new Intent(
                    Constants.ACTION_DOWNLOAD_NETWORK_CONFIRMED);
            intent.putExtra(Constants.CONFIRM_TO_DOWNLOAD, mConfirmToDownload);
            sendBroadcast(intent);
        }
    }

/*
** close dialog with a confirm result.
** confirmToDownload: (-1, 0, 1)
*/
    public static void closeAlertDialogWithConfirm(int confirmToDownload) {
        Log.d(TAG, "closeAlertDialogWithConfirm  confirmToDownload: " + confirmToDownload);
        if (sInstance != null && !sInstance.isFinishing()) {
            mConfirmToDownload = confirmToDownload;
            sInstance.finish();
        }
    }

    public static boolean isConfirmAlertDialogExist() {
        boolean isExist = false;
        if (sInstance != null && !sInstance.isFinishing()) {
            isExist = true;
        }
        return isExist;
    }
}
