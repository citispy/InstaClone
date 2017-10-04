package za.co.myconcepts.instaclone.activities;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import za.co.myconcepts.instaclone.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class LoginTest {

    @Rule
    public ActivityTestRule<Login> mActivityRule = new ActivityTestRule<>(Login.class);

    @Test
    public void testLogin() {
        onView(withId(R.id.etUsername)).perform(typeText("yusuf"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText("321"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        onView(withId(R.id.etPassword)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText("123"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText("Profile")).perform(click());
        onView(withId(R.id.btnLogout)).perform(click());
    }

}