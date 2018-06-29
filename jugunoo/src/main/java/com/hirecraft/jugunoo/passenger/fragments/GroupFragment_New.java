package com.hirecraft.jugunoo.passenger.fragments;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hirecraft.jugunoo.passenger.CompanyListActivity;
import com.hirecraft.jugunoo.passenger.R;
import com.hirecraft.jugunoo.passenger.adapter.CustomGroupListAdapter;
import com.hirecraft.jugunoo.passenger.adapter.GroupListModel;
import com.hirecraft.jugunoo.passenger.common.Constant;
import com.hirecraft.jugunoo.passenger.common.ConstantMessages;
import com.hirecraft.jugunoo.passenger.common.Function;
import com.hirecraft.jugunoo.passenger.common.Global;
import com.hirecraft.jugunoo.passenger.common.TransparentProgressDialog;
import com.hirecraft.jugunoo.passenger.floatingactionbar.FloatingActionButton;
import com.hirecraft.jugunoo.passenger.networkhandler.NetworkHandler;
import com.hirecraft.jugunoo.passenger.utility.SharedPreferencesManager;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class GroupFragment_New extends Fragment
{
	private SharedPreferencesManager mgr;
	private String TAG = GroupFragment_New.class.getSimpleName();

	private GridView gvGroupNames;
	private EditText etGroupNameUserReq;

	private ProgressBar progressBar;
	private ArrayList<HashMap<String, String>> groups = null;

	private Runnable pdRunnable;
	private TransparentProgressDialog pd;
	private Handler pdHandler;

	private String usertype;

	// private TextView groupState;
	// private ImageView addGroups;

	public static int IS_GROUP_ACTION = 0;
	public static boolean isGroupSuccess = false;

	private CustomGroupListAdapter groupNameAdapter;

	ArrayAdapter<String> dataAdapter;

	private LinearLayout llErrorStateFleeetGroup;
	private Button btRetryFleetGroup;

	private static boolean groupfrag_visible;

	// private SwipeRefreshLayout mSwipeRefreshLayout;

	private Handler handlerswipe = new Handler();

	private ArrayList<GroupListModel> groupNameListArray;
	private HashMap<String, String> listSelectItem;

	private Gson gson = new Gson();

	private String[] TitleName =
	{ "Rename Group, Delete Group" };

	private ArrayList<String> array_sort;
	private AlertDialog myalertDialog = null;

	private String selectedGroupId, selectedGroupName, selectedManagerId,
			selectedManagerName;

	private static int REQUEST_CODE_GROUP = 2;

	private Dialog dialogAddNewGroup;

	private FloatingActionButton fab;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_group_new, container, false);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		Crouton.cancelAllCroutons();
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		mgr = new SharedPreferencesManager(getActivity());

		llErrorStateFleeetGroup = (LinearLayout) getActivity().findViewById(
				R.id.llErrorStateFleeetGroup);

		btRetryFleetGroup = (Button) getActivity().findViewById(
				R.id.btRetryFleetGroup);

		gvGroupNames = (GridView) getActivity().findViewById(
				R.id.gridViewGroupList);

		groupNameListArray = new ArrayList<GroupListModel>();

		progressBar = (ProgressBar) getActivity().findViewById(
				R.id.progGroupGroupList);

		progressBar.getIndeterminateDrawable().setColorFilter(
				Color.rgb(155, 219, 70),
				android.graphics.PorterDuff.Mode.MULTIPLY);

		pdHandler = new Handler();

		pd = new TransparentProgressDialog(getActivity(),
				R.drawable.loading_image);

		// groupState = (TextView) getActivity().findViewById(R.id.groupStatus);
		// addGroups = (ImageView) getActivity().findViewById(R.id.addGroup);

		// mSwipeRefreshLayout = (SwipeRefreshLayout)
		// getActivity().findViewById(
		// R.id.group_refresh_layoutGroupList);
		//
		// mSwipeRefreshLayout.setColorSchemeResources(R.color.refresh_yellow,
		// R.color.green, R.color.refresh_yellow, R.color.green);

		progressBar.setVisibility(View.VISIBLE);

		makeGroupNameListReq("A");

		btRetryFleetGroup.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				makeGroupNameListReq("A");
			}
		});

		// mSwipeRefreshLayout
		// .setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
		// {
		// @Override
		// public void onRefresh()
		// {
		// initiateRefresh();
		// }
		// });

		usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
		try
		{
			if (usertype.equalsIgnoreCase("Admin"))
			{
				// addGroups.setVisibility(View.VISIBLE);
			}
			else
			{
				// addGroups.setVisibility(View.INVISIBLE);
			}

			// addGroups.setOnClickListener(new OnClickListener()
			// {
			// @Override
			// public void onClick(View arg0)
			// {
			// if (JugunooUtil.isConnectedToInternet(getActivity()))
			// {
			// startActivity(new Intent(getActivity(),
			// AddFleetGroupActivity.class));
			// }
			// else
			// {
			// Function.showToast(getActivity(),
			// "No internet connection");
			// }
			// }
			// });

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
		fab.attachToListView(gvGroupNames);

		fab.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				if (usertype.equalsIgnoreCase("Admin"))
				{
					Intent intent = new Intent(getActivity(),
							AddFleetGroupActivity_new.class);
					startActivity(intent);
				}
				else
				{
					alertAddNewGroup();
				}
			}
		});

		gvGroupNames.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{

				gridItemOnClicked(position);

				Intent intent = new Intent();
				intent.putExtra("groupId", selectedGroupId);
				intent.putExtra("groupName", selectedGroupName);
				intent.setClass(getActivity(), UsersFragment_New.class);
				startActivity(intent);

			}

		});

		gvGroupNames.setOnItemLongClickListener(new OnItemLongClickListener()
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3)
			{
				gridItemOnClicked(position);

				if (usertype.equalsIgnoreCase("Admin"))
				{
					alertManageGroup();
				}
				else
				{
					alertExitFromGroup();
				}

				return true;
			}
		});
	}

	private void alertManageGroup()
	{
		final Dialog dialogManageGroup = new Dialog(getActivity());
		dialogManageGroup.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogManageGroup
				.setContentView(R.layout.dialog_manage_group_alertdialog);

		TextView tvChoiceTitle = (TextView) dialogManageGroup
				.findViewById(R.id.tvChoiceTitle);
		tvChoiceTitle.setText("Manage Group");

		tvChoiceTitle.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

				dialogManageGroup.dismiss();
				dialogManageGroup.cancel();
				// alertRenameGroup();

				Intent i = new Intent(getActivity(),
						AddFleetGroupActivity_new.class);

				i.putExtra("GroupId", selectedGroupId);
				i.putExtra("GrName", selectedGroupName);

				i.putExtra("ManagingUserId", selectedManagerId);
				i.putExtra("MgrName", selectedManagerName);

				startActivity(i);

			}
		});

		dialogManageGroup.show();
	}

	private void alertAddNewGroup()
	{
		dialogAddNewGroup = new Dialog(getActivity());
		dialogAddNewGroup.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogAddNewGroup.setContentView(R.layout.dialog_rename_group);

		TextView tvHeaderUpdateGroupName = (TextView) dialogAddNewGroup
				.findViewById(R.id.tvHeaderUpdateGroupName);

		etGroupNameUserReq = (EditText) dialogAddNewGroup
				.findViewById(R.id.etGroupNameUserReq);

		CheckBox chkEnableGroupAdmin = (CheckBox) dialogAddNewGroup
				.findViewById(R.id.chkEnableGroupAdmin);
		chkEnableGroupAdmin.setVisibility(View.GONE);

		tvHeaderUpdateGroupName.setText("Add to Group");

		Button btProceedRenameGroup = (Button) dialogAddNewGroup
				.findViewById(R.id.btProceedRenameGroups);
		Button btCancelRenameGroup = (Button) dialogAddNewGroup
				.findViewById(R.id.btCancelRenameGroups);

		etGroupNameUserReq.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{

				String urlGroup = Global.JUGUNOO_WS
						+ "Passenger/GetGroupName?CompanyName=TCS";

				Log.d("urlGroup", "url Group" + urlGroup);
				Intent intentGroup = new Intent(getActivity(),
						CompanyListActivity.class);

				intentGroup.putExtra("searchHint", "Search Groups");
				intentGroup.putExtra("URL", urlGroup);
				intentGroup.putExtra("successMsg",
						Constant.MessageState.GROUP_NAMES_SUCCESS);
				getActivity().startActivityForResult(intentGroup,
						REQUEST_CODE_GROUP);

			}
		});

		btProceedRenameGroup.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String groupName = "", groupAdmin = "";
				groupName = etGroupNameUserReq.getText().toString().trim();

				makeRenameGroupReq(selectedGroupId, groupName, groupAdmin);
				dialogAddNewGroup.cancel();

			}
		});

		btCancelRenameGroup.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialogAddNewGroup.cancel();
			}
		});

		dialogAddNewGroup.show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_GROUP)
		{
			String groupName = data.getStringExtra("message");
			etGroupNameUserReq.setText(groupName);
		}
	}

	private void alertExitFromGroup()
	{

		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_common_alert);

		TextView popupHeader = (TextView) dialog
				.findViewById(R.id.tvAlertHeader);
		TextView popupMsg = (TextView) dialog.findViewById(R.id.tvAlertMsg);

		Button btOk = (Button) dialog.findViewById(R.id.btAlertOk);
		Button btCancel = (Button) dialog.findViewById(R.id.btAlertCancel);

		popupMsg.setText("Are you sure want to exit from this group ?");
		popupHeader.setText(ConstantMessages.MSG91);

		btOk.setText(ConstantMessages.MSG93);
		btCancel.setText(ConstantMessages.MSG96);
		btCancel.setVisibility(View.VISIBLE);

		btOk.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				makeRequestToExitFromGroup(mgr
						.GetValueFromSharedPrefs("UserID"));
				dialog.cancel();
			}
		});

		btCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.cancel();
			}
		});

		dialog.show();
	}

	private void makeRenameGroupReq(String groupId, String groupName,
			String groupAdmin)
	{

	}

	private void makeRequestToExitFromGroup(String groupId)
	{

	}

	public void gridItemOnClicked(int position)
	{
		for (int i = 0; i < groupNameListArray.size(); i++)
		{
			if (i == position)
			{
				listSelectItem = new HashMap<String, String>();

				GroupListModel model = groupNameListArray.get(i);

				listSelectItem.put("GroupId", model.getGroupId());
				listSelectItem.put("GroupName", model.getGroupName());

				selectedGroupId = model.getGroupId();
				selectedGroupName = model.getGroupName();

				if (usertype.equalsIgnoreCase("Admin"))
				{
					selectedManagerId = model.getManagingUserId();
					selectedManagerName = model.getManagerName();
				}

				Log.d("selectedGroupId", "selectedGroupId " + selectedGroupId);
				Log.d("selectedUserId",
						"selectedUserId "
								+ mgr.GetValueFromSharedPrefs("UserId"));
				Log.d("selectedGroupName", "selectedGroupName "
						+ selectedGroupName);

				Log.d("list item selected item", "Selected item -->"
						+ listSelectItem);
				break;
			}
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if (Global.IsGroupstatUpdated)
		{
			makeGroupNameListReq("A");
			Global.IsGroupstatUpdated = false;
		}
	}

	private void initiateRefresh()
	{
		Log.d("initiateRefresh", "initiateRefresh");
		try
		{
			handlerswipe.postDelayed(new Runnable()
			{
				public void run()
				{
					try
					{
						ServiceCallSpinner("All");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					// mSwipeRefreshLayout.setRefreshing(false);
				}
			}, 2000);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		groupfrag_visible = isVisibleToUser;
		if (groupfrag_visible)
		{
			try
			{
				// groupList.setSelection(0);
				gvGroupNames.setSelection(0);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			// fragment is no longer visible
		}
	}

	private void manageView(boolean Netstatus)
	{
		if (Netstatus)
		{
			gvGroupNames.setVisibility(View.VISIBLE);
			llErrorStateFleeetGroup.setVisibility(View.GONE);
		}
		else
		{
			gvGroupNames.setVisibility(View.GONE);
			llErrorStateFleeetGroup.setVisibility(View.VISIBLE);
		}
	}

	private void ServiceCallSpinner(String groupspinner_pos)
	{
		if (groupspinner_pos.equalsIgnoreCase("All"))
		{
			makeGroupNameListReq("B");
		}
		else if (groupspinner_pos.equalsIgnoreCase("Active"))
		{
			makeGroupNameListReq("A");
		}
		else if (groupspinner_pos.equalsIgnoreCase("Inactive"))
		{
			makeGroupNameListReq("N");
		}
		else if (groupspinner_pos.equalsIgnoreCase("Pending"))
		{
			makeGroupNameListReq("P");
		}
	}

	private void makeGroupNameListReq(String status)
	{
		Log.d("makeGroupNameListReq", "makeGroupNameListReq called");

		String userID = mgr.GetValueFromSharedPrefs("UserID");
		Log.e("User id", userID);
		String usertype = mgr.GetValueFromSharedPrefs("FleetUserType");
		NetworkHandler.GetGroups(TAG, handlerGroupNameList, userID, status);
	}

	Handler handlerGroupNameList = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.arg1)
			{
				case Constant.MessageState.FLEET_GROUP_SUCCESS:
					try
					{
						manageView(true);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					progressBar.setVisibility(View.GONE);
					parseGroupList((JSONObject) msg.obj);
					break;

				case Constant.MessageState.FLEET_GROUP_FAIL:
					try
					{
						manageView(false);
						progressBar.setVisibility(View.GONE);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					// Function.showToast(getActivity(),
					// Constant.network_err_msg);
					break;

			}
		}
	};

	private void parseGroupList(JSONObject obj)
	{
		Log.d("parseGroupUserList", " parseGroupUserList=" + obj.toString());
		try
		{
			if (obj.getString("Result").equalsIgnoreCase("Pass"))
			{
				Log.i(TAG,
						"userType -->"
								+ mgr.GetValueFromSharedPrefs(Constant.USER_TYPE));
				try
				{
					// Condition to check userType
					if (!mgr.GetValueFromSharedPrefs("UserType")
							.equalsIgnoreCase("UserType"))
					{
						mgr.SaveValueToSharedPrefs(Constant.USER_TYPE,
								obj.getString("UserType"));
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				gvGroupNames.setVisibility(View.VISIBLE);
				JSONArray array = obj.getJSONArray("GroupArray");

				if (array.length() != 0)
				{
					Log.d("parseGroupList", "parseGroupList if");
					for (int i = 0; i < array.length(); i++)
					{
						JSONObject c = array.getJSONObject(i);
						GroupListModel logModel = gson.fromJson(c.toString(),
								GroupListModel.class);

						groupNameListArray.add(logModel);

						Log.d("passenger log", "addr size="
								+ groupNameListArray.size());
					}
					listAdapter(groupNameListArray);
				}
				else
				{
					Log.d("parseGroupList", "parseGroupList if");
					Function.showToast(getActivity(),
							"You are yet to Ride on JUGUNOO");
				}
			}
			else
			{
				Log.d("parseGroupList", "parseGroupList fail");
				isGroupSuccess = false;
				// groupState.setVisibility(View.VISIBLE);
				// groupState.setText(obj.getString("Message"));
				groups.clear();

				// groupList.setAdapter(null);
				gvGroupNames.setAdapter(null);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void listAdapter(ArrayList<GroupListModel> tripLogsArray)
	{
		// gridView adapter to populate data to listView

		Log.d("listAdapter", "listAdapter called");

		groupNameAdapter = new CustomGroupListAdapter(getActivity(),
				R.layout.template_groups, tripLogsArray);

		gvGroupNames.setAdapter(groupNameAdapter);

		if (gvGroupNames.getAdapter().getCount() > 0)
		{
			// Function.showToast(getActivity(), "No groups details found");
			// textView.setVisibility(View.GONE);
		}

	}

	@Override
	public void onStop()
	{
		super.onStop();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

}
