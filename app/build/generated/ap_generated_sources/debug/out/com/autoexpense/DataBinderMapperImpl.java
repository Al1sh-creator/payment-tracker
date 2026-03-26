package com.autoexpense;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import androidx.databinding.DataBinderMapper;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import com.autoexpense.databinding.ActivityAddTransactionBindingImpl;
import com.autoexpense.databinding.ActivityMainBindingImpl;
import com.autoexpense.databinding.ActivityPrivacyNoticeBindingImpl;
import com.autoexpense.databinding.ActivityScannerBindingImpl;
import com.autoexpense.databinding.ActivitySettingsBindingImpl;
import com.autoexpense.databinding.ActivitySplitBillBindingImpl;
import com.autoexpense.databinding.ActivitySubscriptionsBindingImpl;
import com.autoexpense.databinding.ActivityTransactionListBindingImpl;
import com.autoexpense.databinding.ItemTransactionBindingImpl;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBinderMapperImpl extends DataBinderMapper {
  private static final int LAYOUT_ACTIVITYADDTRANSACTION = 1;

  private static final int LAYOUT_ACTIVITYMAIN = 2;

  private static final int LAYOUT_ACTIVITYPRIVACYNOTICE = 3;

  private static final int LAYOUT_ACTIVITYSCANNER = 4;

  private static final int LAYOUT_ACTIVITYSETTINGS = 5;

  private static final int LAYOUT_ACTIVITYSPLITBILL = 6;

  private static final int LAYOUT_ACTIVITYSUBSCRIPTIONS = 7;

  private static final int LAYOUT_ACTIVITYTRANSACTIONLIST = 8;

  private static final int LAYOUT_ITEMTRANSACTION = 9;

  private static final SparseIntArray INTERNAL_LAYOUT_ID_LOOKUP = new SparseIntArray(9);

  static {
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.autoexpense.R.layout.activity_add_transaction, LAYOUT_ACTIVITYADDTRANSACTION);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.autoexpense.R.layout.activity_main, LAYOUT_ACTIVITYMAIN);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.autoexpense.R.layout.activity_privacy_notice, LAYOUT_ACTIVITYPRIVACYNOTICE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.autoexpense.R.layout.activity_scanner, LAYOUT_ACTIVITYSCANNER);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.autoexpense.R.layout.activity_settings, LAYOUT_ACTIVITYSETTINGS);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.autoexpense.R.layout.activity_split_bill, LAYOUT_ACTIVITYSPLITBILL);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.autoexpense.R.layout.activity_subscriptions, LAYOUT_ACTIVITYSUBSCRIPTIONS);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.autoexpense.R.layout.activity_transaction_list, LAYOUT_ACTIVITYTRANSACTIONLIST);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.autoexpense.R.layout.item_transaction, LAYOUT_ITEMTRANSACTION);
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View view, int layoutId) {
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = view.getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
        case  LAYOUT_ACTIVITYADDTRANSACTION: {
          if ("layout/activity_add_transaction_0".equals(tag)) {
            return new ActivityAddTransactionBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_add_transaction is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYMAIN: {
          if ("layout/activity_main_0".equals(tag)) {
            return new ActivityMainBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_main is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYPRIVACYNOTICE: {
          if ("layout/activity_privacy_notice_0".equals(tag)) {
            return new ActivityPrivacyNoticeBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_privacy_notice is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYSCANNER: {
          if ("layout/activity_scanner_0".equals(tag)) {
            return new ActivityScannerBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_scanner is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYSETTINGS: {
          if ("layout/activity_settings_0".equals(tag)) {
            return new ActivitySettingsBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_settings is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYSPLITBILL: {
          if ("layout/activity_split_bill_0".equals(tag)) {
            return new ActivitySplitBillBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_split_bill is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYSUBSCRIPTIONS: {
          if ("layout/activity_subscriptions_0".equals(tag)) {
            return new ActivitySubscriptionsBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_subscriptions is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYTRANSACTIONLIST: {
          if ("layout/activity_transaction_list_0".equals(tag)) {
            return new ActivityTransactionListBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_transaction_list is invalid. Received: " + tag);
        }
        case  LAYOUT_ITEMTRANSACTION: {
          if ("layout/item_transaction_0".equals(tag)) {
            return new ItemTransactionBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for item_transaction is invalid. Received: " + tag);
        }
      }
    }
    return null;
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View[] views, int layoutId) {
    if(views == null || views.length == 0) {
      return null;
    }
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = views[0].getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
      }
    }
    return null;
  }

  @Override
  public int getLayoutId(String tag) {
    if (tag == null) {
      return 0;
    }
    Integer tmpVal = InnerLayoutIdLookup.sKeys.get(tag);
    return tmpVal == null ? 0 : tmpVal;
  }

  @Override
  public String convertBrIdToString(int localId) {
    String tmpVal = InnerBrLookup.sKeys.get(localId);
    return tmpVal;
  }

  @Override
  public List<DataBinderMapper> collectDependencies() {
    ArrayList<DataBinderMapper> result = new ArrayList<DataBinderMapper>(1);
    result.add(new androidx.databinding.library.baseAdapters.DataBinderMapperImpl());
    return result;
  }

  private static class InnerBrLookup {
    static final SparseArray<String> sKeys = new SparseArray<String>(1);

    static {
      sKeys.put(0, "_all");
    }
  }

  private static class InnerLayoutIdLookup {
    static final HashMap<String, Integer> sKeys = new HashMap<String, Integer>(9);

    static {
      sKeys.put("layout/activity_add_transaction_0", com.autoexpense.R.layout.activity_add_transaction);
      sKeys.put("layout/activity_main_0", com.autoexpense.R.layout.activity_main);
      sKeys.put("layout/activity_privacy_notice_0", com.autoexpense.R.layout.activity_privacy_notice);
      sKeys.put("layout/activity_scanner_0", com.autoexpense.R.layout.activity_scanner);
      sKeys.put("layout/activity_settings_0", com.autoexpense.R.layout.activity_settings);
      sKeys.put("layout/activity_split_bill_0", com.autoexpense.R.layout.activity_split_bill);
      sKeys.put("layout/activity_subscriptions_0", com.autoexpense.R.layout.activity_subscriptions);
      sKeys.put("layout/activity_transaction_list_0", com.autoexpense.R.layout.activity_transaction_list);
      sKeys.put("layout/item_transaction_0", com.autoexpense.R.layout.item_transaction);
    }
  }
}
