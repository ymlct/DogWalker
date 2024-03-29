package com.yaroslavm87.dogwalker.viewModel;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.yaroslavm87.dogwalker.model.Dog;
import com.yaroslavm87.dogwalker.view.WalkRecordListItem;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;


public class Tools {

    private static String LOG_TAG = "myLogs";;


    public static void setColorToViewsDependingOnLastTimeWalk(long lastTimeWalkExact, View ...views) {

        long currentTimeExact = System.currentTimeMillis();
        long currentTimeInFullDays = currentTimeExact - (currentTimeExact % Constants.PERIOD_ONE_DAY);
        long lastTimeWalkInFullDays = lastTimeWalkExact - (lastTimeWalkExact % Constants.PERIOD_ONE_DAY);
        long timeDifference = currentTimeInFullDays - lastTimeWalkInFullDays;

        if(lastTimeWalkExact == 0L || timeDifference >= Constants.PERIOD_FIVE_DAYS) {

            for(View v : views) {
                v.setBackgroundResource(Constants.COLOR_DOG_DID_NOT_WALK_THIS_WEEK);
            }

        } else {

            if(timeDifference >= Constants.PERIOD_THREE_DAYS) {

                for(View v : views) {
                    v.setBackgroundResource(Constants.COLOR_DOG_WALKED_THIS_WEEK);
                }

            } else {

                for(View v : views) {
                    v.setBackgroundResource(Constants.COLOR_DOG_WALKED_RECENTLY);
                }
            }
        }
    }

    public static void setColorTextToViewsDependingOnLastTimeWalk(Context ctx, long lastTimeWalkExact, TextView...views) {

        long currentTimeExact = System.currentTimeMillis();
        long currentTimeInFullDays = currentTimeExact - (currentTimeExact % Constants.PERIOD_ONE_DAY);
        long lastTimeWalkInFullDays = lastTimeWalkExact - (lastTimeWalkExact % Constants.PERIOD_ONE_DAY);
        long timeDifference = currentTimeInFullDays - lastTimeWalkInFullDays;

        if(lastTimeWalkExact == 0L || timeDifference >= Constants.PERIOD_FIVE_DAYS) {

            for(TextView v : views) {
                v.setTextColor(ctx.getColor(Constants.COLOR_DOG_DID_NOT_WALK_THIS_WEEK));
            }

        } else {

            if(timeDifference >= Constants.PERIOD_THREE_DAYS) {

                for(TextView v : views) {
                    v.setTextColor(ctx.getColor(Constants.COLOR_DOG_WALKED_THIS_WEEK));
                }

            } else {

                for(TextView v : views) {
                    v.setTextColor(ctx.getColor(Constants.COLOR_DOG_WALKED_RECENTLY));
                }
            }
        }
    }

    public static void setSystemBarColor(Activity act, @ColorRes int color) {
        Window window = act.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(act.getResources().getColor(color));
    }

    public static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static void loadImageWithGlide(Fragment fragment, Dog dog, ImageView profilePic, int placeholderId) {
        Glide.with(fragment)
                .load(dog.getImageUri())
                .timeout(60000)
                //.placeholder(placeholderId)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(
                            @Nullable GlideException e,
                            Object model,
                            Target<Drawable> target,
                            boolean isFirstResource
                    ) {
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(
                            Drawable resource,
                            Object model,
                            Target<Drawable> target,
                            DataSource dataSource,
                            boolean isFirstResource
                    ) {
                        return false;
                    }
                }).into(profilePic);
    }



    private static final long FOUR_YEAR_PERIOD = 126230400000L;  // 365 + 365 + 366 + 365
    private static final long COMMON_YEAR_PERIOD = 31536000000L; // 365
    private static final long LEAP_YEAR_PERIOD = 31_622_400_000L;   // 366
    private static final int YEARS_IN_FOUR_YEAR_PERIOD = 4;
    private static final int YEARS_BEFORE_SECOND_YEAR_IN_FOUR_YEAR_PERIOD = 1;
    private static final int YEARS_BEFORE_LEAP_YEAR_IN_FOUR_YEAR_PERIOD = 2;
    private static final int YEARS_BEFORE_LAST_YEAR_IN_FOUR_YEAR_PERIOD = 3;
    private static final int START_YEAR = 1970;
    private static  final long DAY_PERIOD = 86400000L;

    public static ArrayList<WalkRecordListItem> generateWalkCalendar(long[] walkTimestamps) {

        ArrayList<WalkRecordListItem> calendar = new ArrayList<>();

        long firstTs = walkTimestamps[0];
        long lastTs = walkTimestamps[walkTimestamps.length - 1];

        int[] allMonthDaysInYStart = getDaysForEachMonthInYear(firstTs);
        int[] allMonthDaysInYFinish = getDaysForEachMonthInYear(lastTs);

        int startMonth = getMonth(firstTs);
        long globalStart = getMomentOfStartMonth(firstTs);

        int finishMonth = getMonth(lastTs);
        long globalFinish = getMomentOfStartMonth(lastTs) + (allMonthDaysInYFinish[finishMonth - 1] * DAY_PERIOD);

        int curIterYearNumber = getYear(firstTs);
        int curIterMonthNumber = startMonth;
        long curIterMonthStart = globalStart;
        int curIterWeekDay = getWeekDay(curIterMonthStart);
        int[] curIterAllMonthDaysInY = allMonthDaysInYStart;
        long curIterMonthLength = curIterAllMonthDaysInY[startMonth - 1] * DAY_PERIOD;
        ArrayList<Integer> curIterWalkDaysInMonth = new ArrayList<>();

        WalkRecordListItem titleItem;
        WalkRecordListItem contentItem;

        do{
            // add item as a month title
            titleItem = new WalkRecordListItem();
            titleItem.setYearTitle(curIterYearNumber);
            titleItem.setMonthTitle(curIterMonthNumber);
            calendar.add(titleItem);

            // add item as set of days for current month
            contentItem = new WalkRecordListItem();
            contentItem.setAllDaysInMonth(matchMonthAndWeekDays_dayNumber(
                    curIterAllMonthDaysInY[curIterMonthNumber - 1],
                    curIterWeekDay));

            // set a list of walks in current month
            curIterWalkDaysInMonth.clear();
            for (long walk : walkTimestamps) {
                if (
                        walk >= curIterMonthStart
                        && walk < curIterMonthStart + curIterMonthLength
                ) {
                    curIterWalkDaysInMonth.add(getDay(walk));
                }
            }
            contentItem.setWalkDays(
                    curIterWalkDaysInMonth.toArray(new Integer[0])
            );
            calendar.add(contentItem);

            // set variables for next iteration
            // next month number
            curIterMonthNumber = curIterMonthNumber + 1;
            if (curIterMonthNumber > 12) {
                curIterYearNumber = getYear(curIterMonthStart + curIterMonthLength);
                curIterMonthNumber = 1;
                // if next year get new set of days
                curIterAllMonthDaysInY = getDaysForEachMonthInYear(curIterMonthStart + curIterMonthLength);
            }
            // next month start moment
            curIterMonthStart = curIterMonthStart + curIterMonthLength;
            // next month length
            curIterMonthLength = curIterAllMonthDaysInY[curIterMonthNumber - 1] * DAY_PERIOD;
            // next month week day start number
            curIterWeekDay = getWeekDay(curIterMonthStart);

        } while (curIterMonthStart < globalFinish);

        return calendar;
    }

    public static void printCalendar(ArrayList<WalkRecordListItem> calendar) {
        Log.d(LOG_TAG,"--------------------------------");
        String[] monthNameSet = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        StringBuilder builder = new StringBuilder();

        for (WalkRecordListItem item : calendar) {

            if (item.isTitle()) {
                builder.append("...\n").append(monthNameSet[item.getMonthTitle() - 1]).append("\n");

            } else {
                int dayCounter = 1;
                String msg = "";

                for (int day : item.getSetOfDays()) {
                    if(day == 0) continue;
                    for (int w : item.getWalkDays()) {
                        if (dayCounter == w) {
                            msg = "w";
                            break;
                        }
                    }

                    builder.append(dayCounter).append(msg).append("  ");
                    msg = "";
                    dayCounter++;
                }

                Log.d(LOG_TAG, builder.toString());
            }
        }
        Log.d(LOG_TAG,"--------------------------------");
    }

    public static boolean isMomentInLeapY(long moment) {
        final long MOMENT_WITH_TZ_OFFSET = TimeZone.getDefault().getOffset(moment) + moment;

        long from4YPeriodEnd = MOMENT_WITH_TZ_OFFSET % FOUR_YEAR_PERIOD;
        return from4YPeriodEnd >= COMMON_YEAR_PERIOD * 2 && from4YPeriodEnd < COMMON_YEAR_PERIOD * 2 + LEAP_YEAR_PERIOD;
    }

    public static long getMomentOfStartYear(long moment) {
        final long MOMENT_WITH_TZ_OFFSET = TimeZone.getDefault().getOffset(moment) + moment;

        long from4YPeriodEnd = MOMENT_WITH_TZ_OFFSET % FOUR_YEAR_PERIOD;
        long endOf4YPeriod = MOMENT_WITH_TZ_OFFSET - from4YPeriodEnd;
        long yearStart;

        if (isMomentInLeapY(moment)) {
            yearStart = (COMMON_YEAR_PERIOD * 2) + endOf4YPeriod;
        } else {
            if ((from4YPeriodEnd - (COMMON_YEAR_PERIOD * 2)) > 0) {
                yearStart = (COMMON_YEAR_PERIOD * 2) + LEAP_YEAR_PERIOD + endOf4YPeriod;
            } else {
                yearStart =
                        (from4YPeriodEnd - COMMON_YEAR_PERIOD) > 0
                                ? COMMON_YEAR_PERIOD + endOf4YPeriod
                                : endOf4YPeriod;
            }
        }
        return yearStart;
    }

    public static int getYear(long moment) {
        final long MOMENT_WITH_TZ_OFFSET = TimeZone.getDefault().getOffset(moment) + moment;

        long from4YPeriodEnd = MOMENT_WITH_TZ_OFFSET % FOUR_YEAR_PERIOD;
        int yrsTill4YEnd = (int) (((MOMENT_WITH_TZ_OFFSET - from4YPeriodEnd) / FOUR_YEAR_PERIOD) * YEARS_IN_FOUR_YEAR_PERIOD);
        int currentY;
        if (isMomentInLeapY(moment)) {
            currentY = START_YEAR + yrsTill4YEnd + YEARS_BEFORE_LEAP_YEAR_IN_FOUR_YEAR_PERIOD;
        } else {
            if ((from4YPeriodEnd - COMMON_YEAR_PERIOD * 2) > 0) {
                currentY = START_YEAR + yrsTill4YEnd + YEARS_BEFORE_LAST_YEAR_IN_FOUR_YEAR_PERIOD;
            } else {
                currentY =
                        (from4YPeriodEnd - COMMON_YEAR_PERIOD) > 0 ?
                                START_YEAR + yrsTill4YEnd + YEARS_BEFORE_SECOND_YEAR_IN_FOUR_YEAR_PERIOD
                                : START_YEAR + yrsTill4YEnd;
            }
        }
        return currentY;
    }

    public static int[] getDaysForEachMonthInYear(long moment) {
        int[] daySet = {31, 0, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        daySet[1] = isMomentInLeapY(moment) ? 29 : 28;
        return daySet;
    }

    public static long getMomentOfStartMonth(long moment) {
        long yearStart = getMomentOfStartYear(moment);
        int[] daySet = getDaysForEachMonthInYear(moment);
        final long MOMENT_WITH_TZ_OFFSET = TimeZone.getDefault().getOffset(moment) + moment;

        long monthStart = yearStart;
        for (int amDays : daySet) {
            long nextMonth = DAY_PERIOD * amDays;
            if (monthStart + nextMonth > MOMENT_WITH_TZ_OFFSET)
                break;
            monthStart = monthStart + nextMonth;
        }
        return monthStart;
    }

    public static int getMonth(long moment) {
        long start = getMomentOfStartYear(moment);
        long monthStart = getMomentOfStartMonth(moment);
        int[] daySet = getDaysForEachMonthInYear(moment);

        int month = 0;
        do {
            if (month == 12)
                break;
            start = start + (daySet[month] * DAY_PERIOD);
            ++month;
        } while (start <= monthStart);
        return month;
    }

    public static long getMomentOfStartDay(long moment) {
        long monthStart = getMomentOfStartMonth(moment);
        final long MOMENT_WITH_TZ_OFFSET = TimeZone.getDefault().getOffset(moment) + moment;

        long dayStart = monthStart;
        for (int day = 1; day <= 31; day++) {
            if (dayStart + DAY_PERIOD > MOMENT_WITH_TZ_OFFSET)
                break;
            dayStart = dayStart + DAY_PERIOD;
        }
        return dayStart;
    }

    public static int getDay(long moment) {
        long start = getMomentOfStartMonth(moment);
        long dayStart = getMomentOfStartDay(moment);

        int day = 0;
        do {
            start = start + DAY_PERIOD;
            ++day;
        } while (start <= dayStart);
        return day;
    }

    public static int getWeekDay(long moment) {
        final int GLOBAL_WEEK_DAY_START = 4;
        final long MOMENT_WITH_TZ_OFFSET = TimeZone.getDefault().getOffset(moment) + moment;

        long beginningOfDay = MOMENT_WITH_TZ_OFFSET - (MOMENT_WITH_TZ_OFFSET % DAY_PERIOD);
        int currentWeekDay = (int)((beginningOfDay / DAY_PERIOD) % 7) + GLOBAL_WEEK_DAY_START;
        if (currentWeekDay > 7) {
            currentWeekDay = currentWeekDay % 7;
        }
        return currentWeekDay;
    }

    public static int[] matchMonthAndWeekDays_dayNumber(int amDaysInMonth, int wdToStartFrom) {
        int prevMonthDays = wdToStartFrom - 1;
        int nextMonthDays = (7 - ((prevMonthDays + amDaysInMonth) % 7) == 7) ?
                0 : 7 - ((prevMonthDays + amDaysInMonth) % 7);
        int amOfCells = prevMonthDays + amDaysInMonth + nextMonthDays;
        int[] weekDaysInMonth = new int[amOfCells];
        for (int i = 0; i < wdToStartFrom - 1; i++) {
            weekDaysInMonth[i] = 0;
        }
        int dayCounter = 1;
        for (int i = wdToStartFrom - 1; i < amDaysInMonth + prevMonthDays; i++) {
            weekDaysInMonth[i] = dayCounter++;
        }
        for (int i = amDaysInMonth + prevMonthDays; i < weekDaysInMonth.length; i++) {
            weekDaysInMonth[i] = 0;
        }
        return weekDaysInMonth;
    }

    public static int[] matchMonthAndWeekDays_weekDayNumber(int amDaysInMonth, int wdToStartFrom) {
        int prevMonthDays = wdToStartFrom - 1;
        int nextMonthDays = (7 - ((prevMonthDays + amDaysInMonth) % 7) == 7) ?
                0 : 7 - ((prevMonthDays + amDaysInMonth) % 7);
        int amOfCells = prevMonthDays + amDaysInMonth + nextMonthDays;
        int[] weekDaysInMonth = new int[amOfCells];
        for (int i = 0; i < wdToStartFrom - 1; i++) {
            weekDaysInMonth[i] = 0;
        }
        for (int i = wdToStartFrom - 1; i < amDaysInMonth + prevMonthDays; i++) {
            weekDaysInMonth[i] = wdToStartFrom;
            wdToStartFrom = (wdToStartFrom == 7 ? 1 : ++wdToStartFrom);
        }
        for (int i = amDaysInMonth + prevMonthDays; i < weekDaysInMonth.length; i++) {
            weekDaysInMonth[i] = 0;
        }
        return weekDaysInMonth;
    }

    // as default adds timezone offset to formatted string date
    public static String parseMillsToDate(long millsToParse, String datePattern) {

        String result;

        Locale russian = new Locale("ru");

        SimpleDateFormat formatter = new SimpleDateFormat(datePattern, russian);

        DateFormatSymbols dateFormatSymbols = DateFormatSymbols.getInstance(russian);
        String[] newMonths = {
                "января", "февраля", "марта", "апреля", "мая", "июня",
                "июля", "августа", "сентября", "октября", "ноября", "декабря"};

        if(millsToParse <= 1000L) {
            result = "0";

        } else {
            result = formatter.format(millsToParse);
        }

        return result;
    }


//    public static void displayImageRound(final Context ctx, final ImageView img, @DrawableRes int drawable) {
//        try {
//            Glide.with(ctx).load(drawable).asBitmap().centerCrop().into(new BitmapImageViewTarget(img) {
//                @Override
//                protected void setResource(Bitmap resource) {
//                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(ctx.getResources(), resource);
//                    circularBitmapDrawable.setCircular(true);
//                    img.setImageDrawable(circularBitmapDrawable);
//                }
//            });
//        } catch (Exception e) {
//        }
//    }
}
//
