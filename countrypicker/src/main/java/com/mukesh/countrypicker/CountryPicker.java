package com.mukesh.countrypicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class CountryPicker extends Fragment implements View.OnClickListener {

  private StickyListHeadersListView countryListView;
  private View closeView;
  private TextView mSelectedTextView;
  private ImageView mSelectedImageView;

  private CountryAdapter adapter;
  private List<Country> countriesList;
  private List<Country> selectedCountriesList;
  private CountryPickerListener listener;
  private Country mSelectedCountry;
  private Country mLocationCountry;
  private View mHeader;
  private Context context;
  private EditText mSearchEditText;

  public static CountryPicker newInstance() {
    return new CountryPicker();
  }

  public CountryPicker() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    context = container.getContext();
    getAllCountries();
    View view = inflater.inflate(R.layout.country_picker, container, false);
    mSearchEditText = (EditText) view.findViewById(R.id.country_code_picker_search);
    countryListView = (StickyListHeadersListView) view.findViewById(R.id.country_code_picker_listview);
    closeView = view.findViewById(R.id.close);
    closeView.setOnClickListener(this);

    selectedCountriesList = new ArrayList<>(countriesList.size());
    selectedCountriesList.addAll(countriesList);

    mHeader = inflater.inflate(R.layout.list_header, null, false);
    mSelectedTextView = (TextView) mHeader.findViewById(R.id.selected_text);
    TextView locationTextView = (TextView) mHeader.findViewById(R.id.location_text);
    mSelectedImageView = (ImageView) mHeader.findViewById(R.id.selected_icon);
    ImageView locationImageView = (ImageView) mHeader.findViewById(R.id.location_icon);

    if (mSelectedCountry == null) {
      mSelectedCountry = mLocationCountry;
    }

    if (mSelectedCountry != null) {
      mSelectedTextView.setText(mSelectedCountry.getName());
      mSelectedImageView.setImageResource(mSelectedCountry.getFlag());
    }
    if (mLocationCountry != null) {
      locationTextView.setText(mLocationCountry.getName());
      locationImageView.setImageResource(mLocationCountry.getFlag());
    }

    adapter = new CountryAdapter(getActivity(), selectedCountriesList);
    countryListView.addHeaderView(mHeader);
    countryListView.setAdapter(adapter);
    countryListView.setAreHeadersSticky(true);
    countryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (listener != null) {
          if (countryListView.getHeaderViewsCount() != 0 && position == 0) {
            return;
          }
          Country country = selectedCountriesList.get(countryListView.getHeaderViewsCount() == 0
                   ? position : position - 1);
          mSelectedCountry = country;
          mSelectedTextView.setText(country.getName());
          mSelectedImageView.setImageResource(country.getFlag());
          reset();
          listener.onSelectCountry(country.getName(), country.getCode(), country.getDialCode(),
                  country.getFlag());
        }
      }
    });

    mSearchEditText.addTextChangedListener(new TextWatcher() {

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        search(s.toString());
      }
    });

    return view;
  }

  public void setListener(CountryPickerListener listener) {
    this.listener = listener;
  }

  @SuppressLint("DefaultLocale")
  private void search(String text) {
    if (!TextUtils.isEmpty(text)) {
      if (countryListView.getHeaderViewsCount() != 0) {
        countryListView.removeHeaderView(mHeader);
      }
    } else {
      if (countryListView.getHeaderViewsCount() == 0) {
        countryListView.addHeaderView(mHeader);
      }
    }
    selectedCountriesList.clear();
    for (Country country : countriesList) {
      if (country.getName().toLowerCase(Locale.ENGLISH).contains(text.toLowerCase())) {
        selectedCountriesList.add(country);
      }
    }
    adapter.notifyDataSetChanged();
  }

  public void getAllCountries() {
    if (countriesList == null) {
      try {
        countriesList = new ArrayList<>();
        String allCountriesCode = readEncodedJsonString();
        JSONArray countryArray = new JSONArray(allCountriesCode);
        for (int i = 0; i < countryArray.length(); i++) {
          JSONObject jsonObject = countryArray.getJSONObject(i);
          String countryDialCode = jsonObject.getString("dial_code");
          String countryCode = jsonObject.getString("code");
          Country country = new Country();
          country.setCode(countryCode);
          country.setDialCode(countryDialCode);
          country.setFlag(getFlagResId(countryCode));
          countriesList.add(country);
        }
        selectedCountriesList = new ArrayList<>();
        selectedCountriesList.addAll(countriesList);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private static String readEncodedJsonString() throws java.io.IOException {
    byte[] data = Base64.decode(Constants.ENCODED_COUNTRY_CODE, Base64.DEFAULT);
    return new String(data, "UTF-8");
  }

  public void setCountriesList(List<Country> newCountries) {
    this.countriesList.clear();
    this.countriesList.addAll(newCountries);
  }

  public void setLocationCountry(Country country) {
    mLocationCountry = country;
  }

  public Country getUserCountryInfo(Context context) {
    this.context = context;
    getAllCountries();
    TelephonyManager telephonyManager =
            (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    if (!(telephonyManager.getSimState() == TelephonyManager.SIM_STATE_ABSENT)) {
      return getCountry(telephonyManager.getSimCountryIso());
    }
    return afghanistan();
  }

  public Country getCountryByLocale( Context context, Locale locale ) {
    this.context = context;
    String countryIsoCode = locale.getISO3Country().substring(0,2).toLowerCase();
    return getCountry(countryIsoCode);
  }

  public Country getCountryByName ( Context context, String countryName ) {
    this.context = context;
    Map<String, String> countries = new HashMap<>();
    for (String iso : Locale.getISOCountries()) {
      Locale l = new Locale("", iso);
      countries.put(l.getDisplayCountry(), iso);
    }

    String countryIsoCode = countries.get(countryName);
    if (countryIsoCode != null) {
      return getCountry(countryIsoCode);
    }
    return afghanistan();
  }

  private Country getCountry( String countryIsoCode ) {
    getAllCountries();
    for (int i = 0; i < countriesList.size(); i++) {
      Country country = countriesList.get(i);
      if (country.getCode().equalsIgnoreCase(countryIsoCode)) {
        country.setFlag(getFlagResId(country.getCode()));
        return country;
      }
    }
    return afghanistan();
  }

  private Country afghanistan() {
    Country country = new Country();
    country.setCode("AF");
    country.setDialCode("+93");
    country.setFlag(R.drawable.flag_af);
    return country;
  }

  private int getFlagResId(String drawable) {
    try {
      return context.getResources()
              .getIdentifier("flag_" + drawable.toLowerCase(Locale.ENGLISH), "drawable",
                      context.getPackageName());
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

  @Override public void onClick(View v) {
    if (v == closeView) {
      reset();
      getActivity().getSupportFragmentManager().popBackStackImmediate();
    }
  }

  private void reset() {
    mSearchEditText.setText("");
    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    if (imm != null) {
      imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
    }
  }
}
