package com.mukeshsolanki.countrypickerexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.CountryPickerListener;

public class MainActivity extends AppCompatActivity {

  private TextView mCountryNameTextView, mCountryIsoCodeTextView, mCountryDialCodeTextView;
  private ImageView mCountryFlagImageView;
  private Button mPickCountryButton;
  private CountryPicker mCountryPicker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initialize();
    setListener();
  }

  private void setListener() {
    mCountryPicker.setListener(new CountryPickerListener() {
      @Override
      public void onSelectCountry(String name, String code, String dialCode,
                                  int flagDrawableResID) {
        mCountryNameTextView.setText(name);
        mCountryIsoCodeTextView.setText(code);
        mCountryDialCodeTextView.setText(dialCode);
        mCountryFlagImageView.setImageResource(flagDrawableResID);
        getSupportFragmentManager().popBackStackImmediate();
      }
    });
    mPickCountryButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        show();
      }
    });
    getUserCountryInfo();
  }

  private void initialize() {
    mCountryNameTextView = (TextView) findViewById(R.id.selected_country_name_text_view);
    mCountryIsoCodeTextView = (TextView) findViewById(R.id.selected_country_iso_text_view);
    mCountryDialCodeTextView = (TextView) findViewById(R.id.selected_country_dial_code_text_view);
    mPickCountryButton = (Button) findViewById(R.id.country_picker_button);
    mCountryFlagImageView = (ImageView) findViewById(R.id.selected_country_flag_image_view);
    mCountryPicker = CountryPicker.newInstance();

    // You can limit the displayed countries
    //ArrayList<Country> nc = new ArrayList<>();
    //for (Country c : Country.getAllCountries()) {
    //  if (c.getDialCode().endsWith("0")) {
    //    nc.add(c);
    //  }
    //}
    // and decide, in which order they will be displayed
    //Collections.reverse(nc);
    //mCountryPicker.setCountriesList(nc);
  }

  private void show() {
    getSupportFragmentManager().beginTransaction()
        .setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top)
        .add(R.id.root, mCountryPicker).addToBackStack(null).commit();
  }

  private void getUserCountryInfo() {
    Country country = mCountryPicker.getUserCountryInfo(getApplicationContext());
    if (country != null) {
      mCountryFlagImageView.setImageResource(country.getFlag());
      mCountryDialCodeTextView.setText(country.getDialCode());
      mCountryIsoCodeTextView.setText(country.getCode());
      mCountryNameTextView.setText(country.getName());
      mCountryPicker.setLocationCountry(country);
    }
  }
}