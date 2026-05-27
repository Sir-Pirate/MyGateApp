package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ──────────────────────────────────────────────
// Data class
// ──────────────────────────────────────────────
data class AppUser(
    val uid: String       = "",
    val name: String      = "",
    val email: String     = "",
    val phone: String     = "",
    val role: String      = "",
    val flatNo: String    = "",
    val tower: String     = ""
)

// ──────────────────────────────────────────────
// Adapter
// ──────────────────────────────────────────────
class UserAdapter(
    private val onEdit: (AppUser) -> Unit
) : RecyclerView.Adapter<UserAdapter.VH>() {

    private val allUsers  = mutableListOf<AppUser>()
    private val displayed = mutableListOf<AppUser>()

    fun submitList(list: List<AppUser>) {
        allUsers.clear()
        allUsers.addAll(list)
        filter("")
    }

    fun filter(query: String) {
        val q = query.trim().lowercase()
        displayed.clear()
        displayed.addAll(
            if (q.isEmpty()) allUsers
            else allUsers.filter {
                it.name.lowercase().contains(q)  ||
                        it.email.lowercase().contains(q) ||
                        it.phone.contains(q)             ||
                        it.flatNo.lowercase().contains(q)||
                        it.role.lowercase().contains(q)
            }
        )
        notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName:    TextView   = v.findViewById(R.id.tvUserName)
        val tvEmail:   TextView   = v.findViewById(R.id.tvUserEmail)
        val tvPhone:   TextView   = v.findViewById(R.id.tvUserPhone)
        val tvFlat:    TextView   = v.findViewById(R.id.tvUserFlat)
        val chipRole:  TextView   = v.findViewById(R.id.chipRole)
        val btnEdit:   ImageButton = v.findViewById(R.id.btnEditUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val u = displayed[pos]
        h.tvName.text = u.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        h.tvEmail.text = if (u.name.isNotEmpty()) u.name else u.email
        h.tvPhone.text = buildString {
            if (u.phone.isNotEmpty()) append(u.phone)
            if (u.phone.isNotEmpty() && u.email.isNotEmpty()) append("  ·  ")
            if (u.email.isNotEmpty()) append(u.email)
        }
        h.tvFlat.text  = if (u.flatNo.isNotEmpty()) u.flatNo else "—"

        h.chipRole.text = u.role.replaceFirstChar { it.uppercase() }

        val (bg, fg) = when (u.role) {
            "admin"    -> Pair(0xFFE8EAF6.toInt(), 0xFF1A237E.toInt())
            "guard"    -> Pair(0xFFE8F5E9.toInt(), 0xFF2E7D32.toInt())
            else       -> Pair(0xFFFBE9E7.toInt(), 0xFFE65100.toInt())
        }
        h.chipRole.setBackgroundColor(bg)
        h.chipRole.setTextColor(fg)

        h.btnEdit.setOnClickListener { onEdit(u) }
    }

    override fun getItemCount() = displayed.size
}

// ──────────────────────────────────────────────
// Activity
// ──────────────────────────────────────────────
class AdminUserManagementActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var searchBar: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tvCount: TextView

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_user_management)

        // Security: verify current user is actually admin
        verifyAdminAccess()

        bindViews()
        setupRecycler()
        setupSearch()
        loadUsers()

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
    }

    // ──────────────────────────────────────────
    // Guard: only admins can be here
    // ──────────────────────────────────────────
    private fun verifyAdminAccess() {
        val uid = auth.currentUser?.uid ?: run { finish(); return }
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.getString("role") != "admin") {
                    Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { finish() }
    }

    private fun bindViews() {
        recyclerView = findViewById(R.id.rvUsers)
        searchBar    = findViewById(R.id.etSearch)
        progressBar  = findViewById(R.id.progressBar)
        tvEmpty      = findViewById(R.id.tvEmpty)
        tvCount      = findViewById(R.id.tvUserCount)
    }

    private fun setupRecycler() {
        adapter = UserAdapter(onEdit = { user -> showEditDialog(user) })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { adapter.filter(s?.toString() ?: "") }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
        })
    }

    // ──────────────────────────────────────────
    // Load all users from Firestore
    // ──────────────────────────────────────────
    private fun loadUsers() {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility     = View.GONE

        db.collection("users")
            .orderBy("name")
            .get()
            .addOnSuccessListener { snapshot ->
                progressBar.visibility = View.GONE

                val users = snapshot.documents.mapNotNull { doc ->
                    AppUser(
                        uid    = doc.id,
                        name   = doc.getString("name")   ?: "",
                        email  = doc.getString("email")  ?: "",
                        phone  = doc.getString("phone")  ?: "",
                        role   = doc.getString("role")   ?: "resident",
                        flatNo = doc.getString("flatNo") ?: "",
                        tower  = doc.getString("tower")  ?: ""
                    )
                }

                tvCount.text = "${users.size} users"
                adapter.submitList(users)

                tvEmpty.visibility =
                    if (users.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to load users: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // ──────────────────────────────────────────
    // Edit Dialog
    // Allows admin to update: name, phone,
    // role, flatNo, tower
    // ──────────────────────────────────────────
    private fun showEditDialog(user: AppUser) {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.dialog_edit_user, null)

        val etName    = view.findViewById<EditText>(R.id.etEditName)
        val etPhone   = view.findViewById<EditText>(R.id.etEditPhone)
        val etFlatNo  = view.findViewById<EditText>(R.id.etEditFlatNo)
        val etTower   = view.findViewById<EditText>(R.id.etEditTower)
        val spinRole  = view.findViewById<Spinner>(R.id.spinEditRole)
        val tilFlat   = view.findViewById<View>(R.id.rowFlat)

        // Pre-fill
        etName.setText(user.name)
        etPhone.setText(user.phone)
        etFlatNo.setText(
            if (user.flatNo.contains("-"))
                user.flatNo.substringAfter("-")
            else user.flatNo
        )
        etTower.setText(user.tower)

        // Role spinner
        val roles = arrayOf("resident", "guard", "admin")
        val spinAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinRole.adapter = spinAdapter
        spinRole.setSelection(roles.indexOf(user.role).coerceAtLeast(0))

        // Show/hide flat row based on role
        // Show flat row immediately based on the loaded user's role,
        // not from the spinner (which hasn't fired onItemSelected yet)
        tilFlat.visibility =
            if (user.role == "resident") View.VISIBLE else View.GONE

        spinRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                tilFlat.visibility =
                    if (roles[pos] == "resident") View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        AlertDialog.Builder(this)
            .setTitle("Edit User")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                saveUserEdits(user, etName, etPhone, etFlatNo, etTower, spinRole)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveUserEdits(
        user: AppUser,
        etName: EditText,
        etPhone: EditText,
        etFlatNo: EditText,
        etTower: EditText,
        spinRole: Spinner
    ) {
        val newName  = etName.text.toString().trim()
        val newPhone = etPhone.text.toString().trim()
        val newRole  = spinRole.selectedItem.toString()
        val flatNo   = etFlatNo.text.toString().trim()
        val tower    = etTower.text.toString().trim().uppercase()

        // Validation
        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPhone.length != 10 || !newPhone.matches("[0-9]+".toRegex())) {
            Toast.makeText(this, "Enter a valid 10-digit phone", Toast.LENGTH_SHORT).show()
            return
        }

        val fullFlatNo = if (newRole == "resident" && tower.isNotEmpty() && flatNo.isNotEmpty())
            "$tower-$flatNo" else ""

        val updates = hashMapOf<String, Any>(
            "name"   to newName,
            "phone"  to newPhone,
            "role"   to newRole,
            "flatNo" to fullFlatNo,
            "tower"  to tower
        )

        db.collection("users").document(user.uid)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "User updated", Toast.LENGTH_SHORT).show()
                loadUsers()   // refresh list
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}