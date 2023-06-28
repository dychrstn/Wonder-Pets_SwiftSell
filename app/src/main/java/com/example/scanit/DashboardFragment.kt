package com.example.scanit

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import kotlin.reflect.typeOf

class DashboardFragment : Fragment() {
    private lateinit var dbItem: DatabaseReference
    private lateinit var transHistoView: RecyclerView
    private lateinit var histoAdapt: transHistoAdapt

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        transHistoView = view.findViewById(R.id.histoTransactView)

        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        transHistoView.layoutManager = layoutManager

        return view
    }

    private lateinit var buyList:ArrayList<transData>
    private lateinit var tempList: ArrayList<tempTrans>
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transHistoView = view.findViewById(R.id.histoTransactView)
        buyList = arrayListOf<transData>()
        tempList = arrayListOf<tempTrans>()
        histoAdapt = transHistoAdapt(buyList){getListBought ->
            openTransList(getListBought)}
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        transHistoView.layoutManager = layoutManager
        transHistoView.adapter = histoAdapt

        val databaseReference = FirebaseDatabase.getInstance().getReference()
        val reference = databaseReference.child("Products")

        // Fetch data from Firebase
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val rowCount = dataSnapshot.childrenCount
                val productin = view.findViewById<TextView>(R.id.textViewQuantity)
                productin.text = "$rowCount"

                var sum = 0
                for (productSnapshot in dataSnapshot.children) {
                    val itemQuantity = productSnapshot.child("itemQuantity").getValue(Int::class.java)
                    if (itemQuantity != null) {
                        sum += itemQuantity
                    }
                }

                val avgstocks = view.findViewById<TextView>(R.id.allstocks)
                avgstocks.text = "$sum"
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        // Fetch transaction history from Firebase
        dbItem = FirebaseDatabase.getInstance().getReference("Order/completeTransactions")
        dbItem.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(transSnap: DataSnapshot) {
                if (transSnap.exists()) {
                    for (childTransId in transSnap.children) {
                        val valTransId = childTransId.key
                        val transHisto = FirebaseDatabase.getInstance()
                            .getReference("Order/completeTransactions/$valTransId")
                        var makeSent = ""
                        var valTot = 0.0
                        var updTot = 0.0
                        var capListPut = 0
                        var setSent = ""
                        var dateProd = ""
                        var timeProd = ""
                        transHisto.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (getTransVal in snapshot.children) {
                                        val childPath = getTransVal.key
                                        if(childPath != "changeGiven" && childPath != "custPay" && childPath != "totalBuy" && childPath != "dateTrans" && childPath != "timeTrans"){

                                            val prodName =
                                                getTransVal.child("itemName").getValue(String::class.java)
                                            val prodQuant =
                                                getTransVal.child("itemQuantity").getValue(String::class.java)
                                            val prodTot =
                                                getTransVal.child("itemTotal").getValue(String::class.java)

                                            if(capListPut < 5){
                                                setSent =
                                                    "${prodName.toString()}...${prodQuant.toString()}...${prodTot.toString()} \n"
                                                capListPut++
                                            }else if(capListPut == 7){
                                                setSent = "..."
                                            }
                                            makeSent = makeSent.plus(setSent)
                                            val itemTotal = prodTot?.toDoubleOrNull() ?: 0.0
                                            updTot = valTot + itemTotal
                                            valTot = updTot

                                        }else{
                                            if(childPath == "dateTrans"){
                                                val dateString = snapshot.child("dateTrans").getValue(String::class.java).toString()
                                                dateProd = dateString // Assign the retrieved date string directly to dateProd
                                            }
                                            if(childPath == "timeTrans"){
                                                timeProd = snapshot.child("timeTrans").getValue(String::class.java).toString()
                                            }
                                        }

                                    }

                                    val item = transData(
                                        valTransId.toString().toInt(),
                                        makeSent,
                                        valTot.toString(),
                                        dateProd,
                                        timeProd
                                    )

                                    buyList.add(item)

                                    tempList.add(tempTrans(valTransId))

                                    histoAdapt.updateItems(buyList)

                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                            }
                        })


                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun openTransList(getListBought: transData){
        val intent = Intent(requireContext(), TransactionActivity::class.java)
        intent.putExtra("getListBought", getListBought.transcationID.toString())
        startActivity(intent)
    }
    companion object {
        // Companion object code
    }
}
