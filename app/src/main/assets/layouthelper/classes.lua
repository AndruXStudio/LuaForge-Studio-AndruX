local bindClass = luajava.bindClass

local function importClass(fullName)
  local ok, cls = pcall(bindClass, fullName)
  if ok and cls ~= nil then
    local shortName = fullName:match("[%w_]+$")
    _G[shortName] = cls
    return true
  end
  return false
end

local classNames = {
  -- AppCompat
  "androidx.appcompat.widget.AppCompatButton",
  "androidx.appcompat.widget.AppCompatEditText",
  "androidx.appcompat.widget.AppCompatTextView",
  "androidx.appcompat.widget.AppCompatImageView",
  "androidx.appcompat.widget.AppCompatCheckBox",
  "androidx.appcompat.widget.AppCompatRadioButton",
  "androidx.appcompat.widget.AppCompatSpinner",
  "androidx.appcompat.widget.AppCompatSeekBar",
  "androidx.appcompat.widget.AppCompatImageButton",
  "androidx.appcompat.widget.AppCompatAutoCompleteTextView",
  "androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView",
  "androidx.appcompat.widget.AppCompatCheckedTextView",
  "androidx.appcompat.widget.AppCompatRatingBar",
  "androidx.appcompat.widget.AppCompatToggleButton",
  "androidx.appcompat.widget.Toolbar",
  "androidx.appcompat.widget.SearchView",
  "androidx.appcompat.widget.SwitchCompat",
  "androidx.appcompat.widget.LinearLayoutCompat",
  "androidx.appcompat.widget.ContentFrameLayout",

  -- Material Design
  "com.google.android.material.button.MaterialButton",
  "com.google.android.material.button.MaterialButtonToggleGroup",
  "com.google.android.material.button.MaterialButtonGroup",
  "com.google.android.material.card.MaterialCardView",
  "com.google.android.material.textfield.TextInputLayout",
  "com.google.android.material.textfield.TextInputEditText",
  "com.google.android.material.textfield.MaterialAutoCompleteTextView",
  "com.google.android.material.textview.MaterialTextView",
  "com.google.android.material.chip.Chip",
  "com.google.android.material.chip.ChipGroup",
  "com.google.android.material.bottomnavigation.BottomNavigationView",
  "com.google.android.material.navigation.NavigationView",
  "com.google.android.material.navigationrail.NavigationRailView",
  "com.google.android.material.tabs.TabLayout",
  "com.google.android.material.tabs.TabItem",
  "com.google.android.material.progressindicator.LinearProgressIndicator",
  "com.google.android.material.progressindicator.CircularProgressIndicator",
  "com.google.android.material.switchmaterial.SwitchMaterial",
  "com.google.android.material.materialswitch.MaterialSwitch",
  "com.google.android.material.checkbox.MaterialCheckBox",
  "com.google.android.material.radiobutton.MaterialRadioButton",
  "com.google.android.material.slider.Slider",
  "com.google.android.material.slider.RangeSlider",
  "com.google.android.material.floatingactionbutton.FloatingActionButton",
  "com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton",
  "com.google.android.material.appbar.AppBarLayout",
  "com.google.android.material.appbar.CollapsingToolbarLayout",
  "com.google.android.material.appbar.MaterialToolbar",
  "com.google.android.material.bottomappbar.BottomAppBar",
  "com.google.android.material.divider.MaterialDivider",
  "com.google.android.material.imageview.ShapeableImageView",
  "com.google.android.material.search.SearchBar",
  "com.google.android.material.search.SearchView",
  "com.google.android.material.loadingindicator.LoadingIndicator",
  "com.google.android.material.floatingtoolbar.FloatingToolbarLayout",
  "com.luaforge.studio.widget.textfield.MaterialTextField",

  -- AndroidX layouts
  "androidx.constraintlayout.widget.ConstraintLayout",
  "androidx.constraintlayout.widget.Guideline",
  "androidx.constraintlayout.widget.Group",
  "androidx.constraintlayout.widget.Placeholder",
  "androidx.coordinatorlayout.widget.CoordinatorLayout",
  "androidx.drawerlayout.widget.DrawerLayout",
  "androidx.gridlayout.widget.GridLayout",
  "androidx.recyclerview.widget.RecyclerView",
  "androidx.swiperefreshlayout.widget.SwipeRefreshLayout",
  "androidx.slidingpanelayout.widget.SlidingPaneLayout",
  "androidx.core.widget.NestedScrollView",
  "androidx.viewpager.widget.ViewPager",
  "androidx.viewpager2.widget.ViewPager2",
  "androidx.cardview.widget.CardView",

  -- AndroLua extras
  "android.widget.PullingLayout",
  "android.widget.HorizontalListView",
  "android.widget.PageView",
  "android.widget.PageLayout",
  "android.widget.ExListView",
  "android.widget.FloatButton",
  "android.widget.CircleImageView",

  -- Framework widgets
  "android.widget.AbsoluteLayout",
  "android.widget.AnalogClock",
  "android.widget.AutoCompleteTextView",
  "android.widget.Button",
  "android.widget.CalendarView",
  "android.widget.CheckBox",
  "android.widget.CheckedTextView",
  "android.widget.Chronometer",
  "android.widget.CompoundButton",
  "android.widget.DatePicker",
  "android.widget.EditText",
  "android.widget.ExpandableListView",
  "android.widget.FrameLayout",
  "android.widget.GridLayout",
  "android.widget.GridView",
  "android.widget.HorizontalScrollView",
  "android.widget.ImageButton",
  "android.widget.ImageView",
  "android.widget.LinearLayout",
  "android.widget.ListPopupWindow",
  "android.widget.ListView",
  "android.widget.MediaController",
  "android.widget.MultiAutoCompleteTextView",
  "android.widget.NumberPicker",
  "android.widget.PopupMenu",
  "android.widget.PopupWindow",
  "android.widget.ProgressBar",
  "android.widget.QuickContactBadge",
  "android.widget.RadioButton",
  "android.widget.RadioGroup",
  "android.widget.RatingBar",
  "android.widget.RelativeLayout",
  "android.widget.RemoteViews",
  "android.widget.ScrollView",
  "android.widget.SeekBar",
  "android.widget.Space",
  "android.widget.Spinner",
  "android.widget.StackView",
  "android.widget.Switch",
  "android.widget.TabHost",
  "android.widget.TabWidget",
  "android.widget.TableLayout",
  "android.widget.TableRow",
  "android.widget.TextClock",
  "android.widget.TextView",
  "android.widget.TimePicker",
  "android.widget.Toast",
  "android.widget.ToggleButton",
  "android.widget.Toolbar",
  "android.widget.VideoView",
  "android.widget.ViewAnimator",
  "android.widget.ViewFlipper",
  "android.widget.ViewSwitcher",
  "android.widget.ZoomButton",
  "android.widget.ZoomControls",
  "android.view.View",
  "android.webkit.WebView",
}

for _, v in ipairs(classNames) do
  importClass(v)
end

-- Aliases used by layout palette / layout tables
pcall(function()
  if SearchView ~= nil then
    MaterialSearchView = SearchView
  end
end)

pcall(function()
  if FlexibleListView == nil and ExListView ~= nil then
    FlexibleListView = ExListView
  end
end)

pcall(function()
  if NumberProgressBar == nil and ProgressBar ~= nil then
    NumberProgressBar = ProgressBar
  end
end)
