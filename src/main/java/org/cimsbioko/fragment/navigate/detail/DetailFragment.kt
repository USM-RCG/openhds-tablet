package org.cimsbioko.fragment.navigate.detail

import androidx.fragment.app.Fragment
import org.cimsbioko.data.DataWrapper

abstract class DetailFragment : Fragment() {
    abstract fun setUpDetails(data: DataWrapper?)
}