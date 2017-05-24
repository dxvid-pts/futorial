package de.davepe.futorial.welcome;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;

import de.davepe.futorial.R;

public class SocialMediaDialog {
    Dialog d;
    Context c;

    public SocialMediaDialog(Context context, final String web, final String youtube, final String facebook, final String soundcloud) {
        c = context;
        d = new Dialog(context);//, R.style.DialogTheme android.R.style.Theme_Translucent_NoTitleBar
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setCancelable(true);
        d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        d.getWindow().setGravity(Gravity.CENTER);
        d.setContentView(R.layout.dialog_social_media);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.show();
        this.c = context;
    }
}
