package com.nimo.fb_effect.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.nimo.facebeauty.FBEffect;
import com.nimo.fb_effect.R;
import com.nimo.fb_effect.base.FBBaseFragment;
import com.nimo.fb_effect.model.FBEventAction;
import com.nimo.fb_effect.model.FBState;
import com.nimo.fb_effect.model.FBViewState;
import com.nimo.fb_effect.utils.FBSelectedPosition;
import com.nimo.fb_effect.view.FBBarView;
import com.shizhefei.view.indicator.FragmentListPageAdapter;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.ScrollIndicatorView;
import com.shizhefei.view.indicator.slidebar.LayoutBar;
import com.shizhefei.view.viewpager.SViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能——人像抠图
 */

public class FBPortraitFragment extends FBBaseFragment {

  private SViewPager htPager;
  private ScrollIndicatorView indicatorView;
  private IndicatorViewPager indicatorViewPager;
  private IndicatorViewPager.IndicatorFragmentPagerAdapter fragmentPagerAdapter;
  private ImageView ivSelected1,ivSelected2,ivSelected3;
  private ImageView ivClean;
  private View container;
  private View line;
  private View divide;
  private FBBarView htBar;

  private final List<String> htTabs = new ArrayList<>();

  @Override protected int getLayoutId() {
    return R.layout.layout_beauty_skin;
  }

  @Override protected void initView(View view, Bundle savedInstanceState) {

    htPager = view.findViewById(R.id.fb_pager);
    indicatorView = view.findViewById(R.id.indicatorView);
    container = view.findViewById(R.id.container);
    line = view.findViewById(R.id.line);
    divide = view.findViewById(R.id.divide);
    htBar = view.findViewById(R.id.fb_bar);
    ivSelected1 = view.findViewById(R.id.iv_selected1);
    ivSelected2 = view.findViewById(R.id.iv_selected2);
    ivSelected3 = view.findViewById(R.id.iv_selected3);
    ivClean= view.findViewById(R.id.iv_clean);
    htBar.setVisibility(View.GONE);
    ivClean.setVisibility(View.GONE);
    divide.setVisibility(View.GONE);




    //添加标题
    htTabs.clear();
//    htTabs.add(requireContext().getString(R.string.portrait_people));
      htTabs.add(requireContext().getString(R.string.portrait_people));

    indicatorView.setSplitAuto(false);
    indicatorViewPager = new IndicatorViewPager(indicatorView, htPager);
    indicatorViewPager.setIndicatorScrollBar(new LayoutBar(getContext(),
        R.layout.layout_indicator_tab));
    htPager.setCanScroll(false);
    htPager.setOffscreenPageLimit(1);
    fragmentPagerAdapter = new IndicatorViewPager.IndicatorFragmentPagerAdapter(getChildFragmentManager()) {
      @Override public int getCount() {
        return htTabs.size();
      }

      @Override public View getViewForTab(int position,
                                          View convertView,
                                          ViewGroup container) {
        convertView = LayoutInflater.from(getContext())
            .inflate(R.layout.item_fb_sticker_tab, container, false);
        ((AppCompatTextView) convertView).setText(htTabs.get(position));
        return convertView;
      }

      @Override
      public int getItemPosition(Object object) {
        return FragmentListPageAdapter.POSITION_NONE;
      }

      @Override public Fragment getFragmentForPage(int position) {
        Log.e("position:", position + "");
          return new PortraitAIFragment();
      }
    };
    indicatorViewPager.setAdapter(fragmentPagerAdapter);
    container.setBackground(ContextCompat.getDrawable(getContext(),
        R.color.dark_background));

    line.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.horizonal_line));
    divide.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.divide_line));
    htPager.setCurrentItem(FBSelectedPosition.POSITION_PORTRAIT,false);
    //changeTheme("");

  }
  /**
   * 更换主题
   */
  @Subscribe(thread = EventThread.MAIN_THREAD,
             tags = { @Tag(FBEventAction.ACTION_CHANGE_THEME) })
  public void changeTheme(@Nullable Object o) {
    Log.e("切换主题:", FBState.isDark ? "黑色" : "白色");

    if (FBState.isDark) {
      container.setBackground(ContextCompat.getDrawable(getContext(),
          R.color.dark_background));

      line.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.divide_line));

    } else {
      container.setBackground(ContextCompat.getDrawable(getContext(),
          R.color.light_background));

      line.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.black_line));

    }
  }



  // /**
  //  * 绿幕背景颜色可选
  //  */
  // @Subscribe(thread = EventThread.MAIN_THREAD,
  //            tags = { @Tag(FBEventAction.ACTION_SHOW_SCREEN_COLOR) })
  // public void showColor(boolean isShow) {
  //   if(isShow){
  //     ivGreen.setVisibility(View.VISIBLE);
  //     ivBlue.setVisibility(View.VISIBLE);
  //     ivWhite.setVisibility(View.VISIBLE);
  //     ivSelect.setVisibility(View.VISIBLE);
  //   }else{
  //     ivGreen.setVisibility(View.GONE);
  //     ivBlue.setVisibility(View.GONE);
  //     ivWhite.setVisibility(View.GONE);
  //     ivSelect.setVisibility(View.GONE);
  //   }
  //
  // }

  @Override public void onDestroyView() {
    super.onDestroyView();
      FBEffect.shareInstance().setAISegEffect("");
      FBSelectedPosition.POSITION_AISEGMENTATION = 0;
  }
}