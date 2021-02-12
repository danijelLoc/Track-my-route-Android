package hr.fer.trackmyroute.api

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.fer.trackmyroute.R

class RoutesAdapter(
    listOfRoutesViewModel: RoutesViewModel,
    onRouteListener: OnRouteListener,
    onRemoveRouteListener: OnRemoveRouteListener
) :
    RecyclerView.Adapter<RoutesAdapter.ViewHolder>() {

    var listOfRoutes: RoutesViewModel = listOfRoutesViewModel
    var onRouteListener: OnRouteListener = onRouteListener
    var onRemoveRouteListener: OnRemoveRouteListener = onRemoveRouteListener

    class ViewHolder(
        itemView: View,
        onRouteListener: OnRouteListener,
        onRemoveRouteListener: OnRemoveRouteListener
    ) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var routeTitleTextView: TextView? = null
        var routeDateTextView: TextView? = null
        var removeButtonView: ImageButton? = null
        var onRouteListener: OnRouteListener? = null
        var onRemoveRouteListener: OnRemoveRouteListener? = null

        init {
            routeTitleTextView = itemView.findViewById(R.id.routeTitleTextView)
            routeDateTextView = itemView.findViewById(R.id.routeDateTextView)
            removeButtonView = itemView.findViewById(R.id.removeButton)
            removeButtonView?.setOnClickListener(this)
            this.onRouteListener = onRouteListener
            this.onRemoveRouteListener = onRemoveRouteListener
            this.routeTitleTextView?.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            if (p0 != null) {
                if (p0.equals(removeButtonView))
                    onRemoveRouteListener?.onRemoveRouteClick(adapterPosition)
                else onRouteListener?.onRouteClick(adapterPosition)
            } else onRouteListener?.onRouteClick(adapterPosition)
        }
    }

    interface OnRouteListener {
        fun onRouteClick(position: Int)
    }

    interface OnRemoveRouteListener {
        fun onRemoveRouteClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val routeListElement = inflater.inflate(R.layout.route_list_element, parent, false)
        return ViewHolder(routeListElement, onRouteListener,onRemoveRouteListener)
    }

    override fun getItemCount(): Int {
        if (listOfRoutes.routeList.value != null) {
            return listOfRoutes.routeList.value!!.count()
        }
        return 0
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (listOfRoutes != null) {
            viewHolder.routeTitleTextView?.text = listOfRoutes.routeList.value!![position].name


            viewHolder.routeDateTextView?.text =
                listOfRoutes.routeList.value!![position].date
        }
    }
}