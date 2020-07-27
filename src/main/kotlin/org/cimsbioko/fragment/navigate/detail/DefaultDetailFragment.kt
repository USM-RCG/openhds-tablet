package org.cimsbioko.fragment.navigate.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cimsbioko.R
import org.cimsbioko.data.DataWrapper

class DefaultDetailFragment : DetailFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.default_detail_fragment, container, false)
    }

    override fun setUpDetails(data: DataWrapper?) {}
}