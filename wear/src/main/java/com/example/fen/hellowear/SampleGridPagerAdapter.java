package com.example.fen.hellowear;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.ImageReference;
import android.view.Gravity;

public class SampleGridPagerAdapter extends FragmentGridPagerAdapter {

    private final Context mContext;

    public SampleGridPagerAdapter(MyActivity myActivity, FragmentManager fragmentManager) {
        super(fragmentManager);
        mContext = myActivity;
    }

    static final int[] BG_IMAGES = new int[] {
            R.drawable.debug_background_1,
            R.drawable.debug_background_2,
            R.drawable.debug_background_3,
            R.drawable.debug_background_4,
            R.drawable.debug_background_5




    };

    /** A simple container for static data in each page */
    private static class Page {
        int titleRes;
        int textRes;
        int iconRes;
        int cardGravity = Gravity.BOTTOM;
        boolean expansionEnabled = true;
        float expansionFactor = 1.0f;
        int expansionDirection = CardFragment.EXPAND_DOWN;

        public Page(int titleRes, int textRes, boolean expansion) {
            this(titleRes, textRes, 0);
            this.expansionEnabled = expansion;
        }

        public Page(int titleRes, int textRes, boolean expansion, float expansionFactor) {
            this(titleRes, textRes, 0);
            this.expansionEnabled = expansion;
            this.expansionFactor = expansionFactor;
        }

        public Page(int titleRes, int textRes, int iconRes) {
            this.titleRes = titleRes;
            this.textRes = textRes;
            this.iconRes = iconRes;
        }

        public Page(int titleRes, int textRes, int iconRes, int gravity) {
            this.titleRes = titleRes;
            this.textRes = textRes;
            this.iconRes = iconRes;
            this.cardGravity = gravity;
        }
    }

    private final Page[][] PAGES = {
            {
                    new Page(R.string.title1, R.string.title1text, R.drawable.bugdroid,
                            Gravity.BOTTOM),
                    new Page(R.string.title1, R.string.title1text, R.drawable.bugdroid,
                            Gravity.BOTTOM)
            },
            {
                    new Page(R.string.title2, R.string.title2text, R.drawable.bugdroid,
                            Gravity.BOTTOM),
                    new Page(R.string.title1, R.string.title1text, R.drawable.bugdroid,
                            Gravity.BOTTOM)
            },
            {
                    new Page(R.string.title3, R.string.title3text, R.drawable.bugdroid,
                            Gravity.BOTTOM),
                    new Page(R.string.title1, R.string.title1text, R.drawable.bugdroid,
                            Gravity.BOTTOM)
            },
            {
                    new Page(R.string.title4, R.string.title4text, R.drawable.bugdroid,
                            Gravity.BOTTOM),
                    new Page(R.string.title1, R.string.title1text, R.drawable.bugdroid,
                            Gravity.BOTTOM)
            },
            {
                    new Page(R.string.title5, R.string.title5text, R.drawable.bugdroid,
                            Gravity.BOTTOM),
                    new Page(R.string.theend, R.string.theendtext, true, 2)
            },

    };

    @Override
    public Fragment getFragment(int row, int col) {
        Page page = PAGES[row][col];

            String title = page.titleRes != 0 ? mContext.getString(page.titleRes) : null;
            String text = page.textRes != 0 ? mContext.getString(page.textRes) : null;
            CardFragment fragment = CardFragment.create(title, text, page.iconRes);
            // Advanced settings
            fragment.setCardGravity(page.cardGravity);
            fragment.setExpansionEnabled(page.expansionEnabled);
            fragment.setExpansionDirection(page.expansionDirection);
            fragment.setExpansionFactor(page.expansionFactor);
            return fragment;
    }

    @Override
    public ImageReference getBackground(int row, int column) {
        return ImageReference.forDrawable(BG_IMAGES[row % BG_IMAGES.length]);
    }

    @Override
    public int getRowCount() {
        return PAGES.length;
    }

    @Override
    public int getColumnCount(int rowNum) {
        return PAGES[rowNum].length;
    }
}