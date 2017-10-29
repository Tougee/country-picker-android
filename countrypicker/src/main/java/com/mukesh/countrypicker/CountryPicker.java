package com.mukesh.countrypicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class CountryPicker extends Fragment implements View.OnClickListener {

  private EditText searchEditText;
  private StickyListHeadersListView countryListView;
  private View closeView;
  private TextView mSelectedTextView;
  private TextView mLocationTextView;
  private ImageView mSelectedImageView;
  private ImageView mLocationImageView;

  private CountryAdapter adapter;
  private List<Country> countriesList = new ArrayList<>();
  private List<Country> selectedCountriesList = new ArrayList<>();
  private CountryPickerListener listener;
  private Context context;
  private Country mSelectedCountry;
  private Country mLocationCountry;

  public static CountryPicker newInstance() {
    Bundle b = new Bundle();
    return new CountryPicker();
  }

  public CountryPicker() {
    setCountriesList(Country.getAllCountries());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.country_picker, container, false);
    searchEditText = (EditText) view.findViewById(R.id.country_code_picker_search);
    countryListView = (StickyListHeadersListView) view.findViewById(R.id.country_code_picker_listview);
    closeView = view.findViewById(R.id.close);
    closeView.setOnClickListener(this);

    selectedCountriesList = new ArrayList<>(countriesList.size());
    selectedCountriesList.addAll(countriesList);

    View header = inflater.inflate(R.layout.list_header, null, false);
    mSelectedTextView = (TextView) header.findViewById(R.id.selected_text);
    mLocationTextView = (TextView) header.findViewById(R.id.location_text);
    mSelectedImageView = (ImageView) header.findViewById(R.id.selected_icon);
    mLocationImageView = (ImageView) header.findViewById(R.id.location_icon);
    if (mSelectedCountry == null) {
      mSelectedCountry = mLocationCountry;
    }
    if (mSelectedCountry != null) {
      mSelectedTextView.setText(mSelectedCountry.getName());
      mSelectedImageView.setImageResource(mSelectedCountry.getFlag());
    }
    if (mLocationCountry != null) {
      mLocationTextView.setText(mLocationCountry.getName());
      mLocationImageView.setImageResource(mLocationCountry.getFlag());
    }

    adapter = new CountryAdapter(getActivity(), selectedCountriesList);
    countryListView.addHeaderView(header);
    countryListView.setAdapter(adapter);
    countryListView.setAreHeadersSticky(true);
    countryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (listener != null) {
          Country country = selectedCountriesList.get(position);
          listener.onSelectCountry(country.getName(), country.getCode(), country.getDialCode(),
              country.getFlag());
          mSelectedCountry = country;
          mSelectedTextView.setText(country.getName());
          mSelectedImageView.setImageResource(country.getFlag());
        }
      }
    });

    searchEditText.addTextChangedListener(new TextWatcher() {

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
    selectedCountriesList.clear();
    for (Country country : countriesList) {
      if (country.getName().toLowerCase(Locale.ENGLISH).contains(text.toLowerCase())) {
        selectedCountriesList.add(country);
      }
    }
    adapter.notifyDataSetChanged();
  }

  public void setCountriesList(List<Country> newCountries) {
    this.countriesList.clear();
    this.countriesList.addAll(newCountries);
  }

  public void setLocationCountry(Country country) {
    mLocationCountry = country;
  }

  @Override public void onClick(View v) {
    if (v == closeView) {
      getActivity().getSupportFragmentManager().popBackStackImmediate();
    }
  }
}
