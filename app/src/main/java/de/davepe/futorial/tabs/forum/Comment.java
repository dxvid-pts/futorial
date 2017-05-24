package de.davepe.futorial.tabs.forum;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.analytics.FirebaseAnalytics;

import de.davepe.futorial.ForumActivity;
import de.davepe.futorial.R;
import de.davepe.futorial.UserData;

/**
 * Created by David on 24.11.2017.
 */

public class Comment {

    ForumActivity a;
    WebView preview;
    EditText mInput;
    String mText = "";
    Dialog dialog;

    int i;
    Integer[] emojicons = new Integer[]{R.drawable.smiley_happy, R.drawable.lol, R.drawable.sad_2, R.drawable.shocked, R.drawable.angry, R.drawable.cry, R.drawable.zinker, R.drawable.zunge,
            R.drawable.surprise, R.drawable.love, R.drawable.heart, R.drawable.cube, R.drawable.key, R.drawable.eq, R.drawable.dj, R.drawable.mixer, R.drawable.vinyl, R.drawable.fucoin,
            R.drawable.eins, R.drawable.zwei, R.drawable.drei, R.drawable.vier, R.drawable.fuenf, R.drawable.sechs, R.drawable.sieben, R.drawable.acht, R.drawable.neun, R.drawable.zehn};
    String[] tooltip = new String[]{"HAPPY", "LOL", "SAD", "SHOCKED", "ANGRY", "CRY", "ZWINKER", "ZUNGE", "SURPRISE", "LOVE", "HEART", "CUBE",
            "KEY", "EQ", "DJ", "MIXER", "VINYL", "FUCOIN", "EINS", "ZWEI", "DREI", "VIER", "FÜNF", "SECHS", "SIEBEN", "ACHT", "NEUN", "ZEHN"};
    String[] links = new String[]{"happy.png", "lol.png", "sad_2.png", "shocked.png", "angry.png", "cry.png", "zinker.png", "zunge.png", "surprise.png", "love.png",
            "heart.png", "cube.png", "key.png", "eq.png", "DJ.png", "mixer.png", "vinyl.png", "FuCoin.png", "1.png", "2.png", "3.png", "4.png", "5.png", "6.png", "7.png", "8.png", "9.png", "10.png"};
    String[] replacements = new String[]{"\uD83D\uDE42", "\uD83D\uDE02", "☹", "\uD83D\uDE12", "\uD83D\uDE20", "\uD83D\uDE25", "\uD83D\uDE0F", "\uD83D\uDE1B", "\uD83D\uDE32",
            "\uD83D\uDE0D", "❤", "\uD83C\uDFB2", "\uD83C\uDFB5", "\uD83D\uDCC8", "\uD83C\uDFA7", "\uD83C\uDF9B️", "\uD83D\uDCBD", "\uD83D\uDEE1️", "1️⃣", "2️⃣", "3️⃣",
            "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "\uD83D\uDD1F"};
    //🙂😂☹️😒😠😥😏😛😲😍❤️🎲🎵📈🎧🎛️💽🛡️1️⃣2️⃣3️⃣4️⃣5️⃣6️⃣7️⃣8️⃣9️⃣🔟🙂😂☹️😒😠😥😏😛😲😍❤️🎲🎵📈🎧🎛️💽🛡️1️⃣2️⃣3️⃣4️⃣5️⃣6️⃣7️⃣8️⃣9️⃣🔟
    LinearLayout mHolder;

    public Comment(final ForumActivity activity, Dialog d) {
        a = activity;
        dialog = d;

        preview = dialog.findViewById(R.id.preview);
        preview.setVisibility(View.VISIBLE);
        preview.getSettings().setJavaScriptEnabled(true);
        preview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        preview.loadUrl("javascript:(function(){document.getElementById('spPostNewButtonBottom').click();var ifr = document.getElementById('postitem_ifr');ifr.style.display = 'block';ifr.style.position = 'fixed';ifr.style.zIndex  = '1';ifr.style.left='0';ifr.style.top='0';ifr.style.width='100%';ifr.style.height='100%';ifr.style.overflow='auto';})()");

        mInput = dialog.findViewById(R.id.edit);

        mInput.post(new Runnable() {
            public void run() {
                mInput.requestFocusFromTouch();
                InputMethodManager lManager = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
                lManager.showSoftInput(mInput, 0);
            }
        });
        dialog.findViewById(R.id.toggleEmojis).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toggled) hideEmojis();
                else addEmojis();
            }
        });
        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int s, int before, int count) {
                String text = cs.toString();
                System.out.println(cs.toString());
                if (before > count) {
                    //   int where = (s == 0 ? count - 1 : s);
                    // System.out.println("DELETE:: " + where + "/" + mInput.getText().toString().length());

                    //  if (cs.charAt(where) == ' ')
                    //      return;
                    //  String sub = text.substring(0, where);
                    // String[] split = sub.split(" ");
                    //  String word = sub.substring(sub.lastIndexOf(' ') + 1);


                   /* int index = text.lastIndexOf(' ');
                    String word = text.substring(index + 1);
                    if (word.startsWith("IMG_")) {
                        String replacement = text.substring(0, index + 1);
                        System.out.println(replacement);
                        mInput.setText(replacement);
                        if (replacement.length() > where)
                            mInput.setSelection(where);
                        else
                            mInput.setSelection(mInput.getText().length());
                    } //delete Emojis instant
                    System.out.println(word);*/
                }
              /*  if (mText.length() < s.length()) {
                    mText = mText + s.charAt(s.length() - 1);
                } else if (mText.length() > s.length()) {
                    mText = mText.substring(0, mText.length() - 1);
                }*/

                // System.out.println("TEXT_: " + s.charAt(s.length() - 1));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // Editable text = mInput.getText();
                mText = mInput.getText().toString();
                // System.out.println("TEXT_: " + text);
                String toHTML = "<!DOCTYPE html><html><body bgcolor=\"#24373E\">" + "<p>" + mText.replaceAll("\n", "</p><p>") + "</p>" + "</body></html>";
                //preview.loadDataWithBaseURL(null, toHTML, "text/html", "UTF-8", null);
                String size = "18px";
                for (int i = 0; i < tooltip.length; i++) {
                    String link = "https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/" + links[i];
                    String rpl = "<img style=\"height: " + size + ";width: " + size + ";padding: 0px; margin: 0px;\" title=\"" + tooltip[i] + "\" alt=\"" + tooltip[i] + "\" src=\"" + link + "\" data-mce-src=\"" + link + "\">";
                    toHTML = toHTML.replaceAll(/*"IMG_" + tooltip[i]*/replacements[i], rpl);
                }
                //https://www.fl-studio-tutorials.de/wp-content/sp-resources/forum-smileys/

                preview.loadUrl("javascript:(function(){var iframe = document.getElementById('postitem_ifr');\n" +
                        "var innerDoc = (iframe.contentDocument) ? iframe.contentDocument : iframe.contentWindow.document;\n" +
                        "var b = innerDoc.getElementById('tinymce');\n" +
                        "b.innerHTML = '" + toHTML + "';})()");//document.getElementById('sfpreview').click();
              /*  preview.loadUrl("javascript:(function(){document.getElementById('sfpreview').click();" +
                        "var ifr = document.getElementById('spPostContent');ifr.style.display = 'block';ifr.style.position = 'fixed';ifr.style.zIndex  = '1';" +
                        "ifr.style.left='0';ifr.style.top='0';ifr.style.width='100%';ifr.style.height='100%';ifr.style.overflow='auto';})()");*/

            }
        });
        dialog.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(mInput.getText().toString());
                //  String toHTML = "<p>" + mInput.toString().replaceAll("\n", "</p><p>") + "</p>";//.getText()
                //  System.out.println(toHTML);
                FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
                Bundle params = new Bundle();
                params.putString("user_name", UserData.getUserName());
                mFirebaseAnalytics.logEvent("comment", params);

                preview.loadUrl("javascript:(function(){document.getElementById('sfsave').click();})()");
                dialog.setCancelable(false);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        a.reloadPage();
                        dialog.dismiss();
                    }
                }, 1000 * 3);
            }
        });
        dialog.show();
    }

    boolean toggled = false;

    private void addEmojis() {
        showBar();
        mHolder = dialog.findViewById(R.id.emojis);
        mHolder.removeAllViews();

        i = 0;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                loop();
            }
        }, 200);
        toggled = true;
    }

    public void loop() {
        try {
            ImageView img = (ImageView) dialog.getLayoutInflater().inflate(R.layout.inflate_image_emoji, null);
            img.setLayoutParams(new ViewGroup.LayoutParams(Math.round(dpFromPx(a.getApplicationContext(), 228f)), ViewGroup.LayoutParams.MATCH_PARENT));
            final int I = i;
            img.setImageResource(emojicons[I]);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addImageBetweentext(((ImageView) v).getDrawable().getConstantState().newDrawable(), /*"IMG_" + tooltip[I]*/replacements[I]);
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                img.setTooltipText(tooltip[i]);
            }
            mHolder.addView(img);
            animate(img);
            if (i < emojicons.length - 1) {
                i++;
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    public void run() {
                        loop();
                    }
                }, 80);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void showBar() {
        View bar = dialog.findViewById(R.id.emoji_holder);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bar.setVisibility(View.VISIBLE);
            int x = bar.getLeft() + 10;
            int y = bar.getBottom();

            int startRadius = 0;
            int endRadius = (int) Math.hypot(dialog.findViewById(R.id.comment_root).getWidth(), dialog.findViewById(R.id.comment_root).getHeight());

            Animator anim = ViewAnimationUtils.createCircularReveal(bar, x, y, startRadius, endRadius);
            anim.setDuration(700);
            anim.start();
        } else {
            bar.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(a, android.R.anim.fade_in);
            animation.setDuration(200);
            bar.startAnimation(animation);
        }
    }

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    protected void animate(final View v) {
        v.clearAnimation();
        ScaleAnimation expand = new ScaleAnimation(0.2f, 1f, 0.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        expand.setDuration(100);     // animation duration in milliseconds
        expand.setInterpolator(new AccelerateInterpolator());
        v.startAnimation(expand);
    }

    public void hideBar() {
        Animation animation = AnimationUtils.loadAnimation(a, android.R.anim.fade_out);
        animation.setDuration(200);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dialog.findViewById(R.id.emoji_holder).setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        dialog.findViewById(R.id.emoji_holder).startAnimation(animation);
    }

    private void hideEmojis() {
        hideBar();
        toggled = false;
    }

    private void addImageBetweentext(Drawable drawable, String replacement) {
        drawable.setBounds(0, 0, mInput.getLineHeight(), mInput.getLineHeight());

        int selectionCursor = mInput.getSelectionStart();
        mInput.getText().insert(selectionCursor, replacement);
        selectionCursor = mInput.getSelectionStart();

        ImageSpan img = new ImageSpan(drawable);

        SpannableStringBuilder builder = new SpannableStringBuilder(mInput.getText());
        builder.setSpan(img, selectionCursor - replacement.length(), selectionCursor, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mInput.setText(builder);
        mInput.setSelection(selectionCursor);
        selectionCursor = mInput.getSelectionStart();
        mInput.getText().insert(selectionCursor, " ");
    }
}
