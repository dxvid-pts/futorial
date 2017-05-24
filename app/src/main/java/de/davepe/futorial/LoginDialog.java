package de.davepe.futorial;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.Window;

public class LoginDialog {

    public LoginDialog(final Context context) {
        Dialog d = new Dialog(context);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setCancelable(true);
        d.setContentView(R.layout.login_dialog);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.show();

        d.findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, LoginActivity.class));
            }
        });
    }
}
