package com.rasalexman.erb.ui.tabs

import androidx.fragment.app.activityViewModels
import androidx.navigation.ui.setupWithNavController
import com.rasalexman.erb.R
import com.rasalexman.erb.databinding.HostFragmentMaintabsBinding
import com.rasalexman.erb.ui.base.BaseHostFragment

class TabsHostFragment : BaseHostFragment<HostFragmentMaintabsBinding, TabHostViewModel>() {

    override val viewModel: TabHostViewModel by activityViewModels()

    override val navControllerId: Int
        get() = R.id.mainTabsHostFragment

    override val layoutId: Int
        get() = R.layout.host_fragment_maintabs

    override fun bindNavController() {
        binding?.bottomNavigationView?.setupWithNavController(navController)
    }
}
