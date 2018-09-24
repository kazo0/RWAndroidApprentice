package com.raywenderlich.placebook.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.PlacePhotoMetadata
import com.google.android.gms.location.places.Places

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.adapter.BookmarkInfoWindowAdapter
import com.raywenderlich.placebook.viewmodel.MapsViewModel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var mapsViewModel: MapsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setupLocationClient()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        getCurrentLocation()

        setupGoogleClient()
        setupMapListeners()
        setupViewModel()
    }

    private fun setupMapListeners() {
        map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        map.setOnPoiClickListener {
            displayPoi(it)
        }
        map.setOnInfoWindowClickListener { handleInfoWindowClick(it) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION -> {
                if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Log.e(TAG, "Location permission denied")
                }
            }
        }
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Log.e(TAG, "Google play connection failed: ${result.errorMessage}")
    }


    private fun setupLocationClient() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
        } else {
            map.isMyLocationEnabled = true

            fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                if (it.result != null) {
                    val latlng = LatLng(it.result.latitude, it.result.longitude)

                    val update = CameraUpdateFactory.newLatLngZoom(latlng, 16.0f)
                    map.moveCamera(update)
                } else {
                    Log.e(TAG, "No location found")
                }
            }
        }
    }

    private fun setupGoogleClient() {
        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Places.GEO_DATA_API)
                .build()
    }

    private fun displayPoi(poi: PointOfInterest) {
        displayPoiGetPlaceStep(poi)
    }

    private fun displayPoiGetPlaceStep(poi: PointOfInterest) {
        Places.GeoDataApi
                .getPlaceById(googleApiClient, poi.placeId)
                .setResultCallback { places ->
                    if (places.status.isSuccess && places.count > 0) {
                        val place = places[0].freeze()
                        displayPoiGetPhotoMetaDataStep(place)
                    } else {
                        Log.e(TAG, "Error with getPlaceById: ${places.status.statusMessage}")
                    }
                    places.release()
                }
    }

    private fun displayPoiGetPhotoMetaDataStep(place: Place) {
        Places.GeoDataApi
                .getPlacePhotos(googleApiClient, place.id)
                .setResultCallback { result ->
                    if (result.status.isSuccess) {
                        val photoMetadataBuffer = result.photoMetadata

                        if (photoMetadataBuffer.count > 0) {
                            val photo = photoMetadataBuffer[0].freeze()
                            displayPoiGetPhotoStep(place, photo)
                        }
                        photoMetadataBuffer.release()
                    }
                }
    }

    private fun displayPoiGetPhotoStep(place: Place, photo: PlacePhotoMetadata) {
        photo.getScaledPhoto(
                googleApiClient,
                resources.getDimensionPixelSize(R.dimen.default_image_width),
                resources.getDimensionPixelSize(R.dimen.default_image_height))
                .setResultCallback { result ->
                    if (result.status.isSuccess) {
                        val image = result.bitmap
                        displayPoiDisplayStep(place, image)
                    } else {
                        displayPoiDisplayStep(place, null)
                    }
                }
    }

    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {
        val marker = map.addMarker(MarkerOptions()
                .position(place.latLng)
                .title(place.name.toString())
                .snippet(place.phoneNumber.toString())
        )
        marker?.tag = PlaceInfo(place, photo)
        marker?.showInfoWindow()
    }

    private fun setupViewModel() {
        mapsViewModel = ViewModelProviders.of(this).get(MapsViewModel::class.java)
        createBookmarkViewObserver()
    }

    private fun handleInfoWindowClick(marker: Marker) {
        when(marker.tag) {
            is MapsActivity.PlaceInfo -> {

                val placeInfo = marker.tag as? PlaceInfo
                if (placeInfo?.place != null && placeInfo.image != null) {
                    launch(CommonPool) {
                        mapsViewModel.addBookmarkFromPlace(placeInfo.place, placeInfo.image)
                    }
                }
                marker.remove()
            }
            is MapsViewModel.BookmarkView -> {
                val bookmarkView = marker.tag as? MapsViewModel.BookmarkView
                marker.hideInfoWindow()
                bookmarkView?.id?.let { startBookmarkDetails(it) }
            }
        }
    }

    private fun addPlaceMarker(bookmark: MapsViewModel.BookmarkView): Marker? {
        val marker = map.addMarker(MarkerOptions()
                .position(bookmark.location)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title(bookmark.name)
                .snippet(bookmark.phone)
                .alpha(0.8f)
        )

        marker.tag = bookmark
        return marker
    }

    private fun displayAllBookmarks(bookmarks: List<MapsViewModel.BookmarkView>) {
        for (bookmark in bookmarks) {
            addPlaceMarker(bookmark)
        }
    }

    private fun createBookmarkViewObserver() {
        mapsViewModel.getBookmarkViews()?.observe(this,
                android.arch.lifecycle.Observer<List<MapsViewModel.BookmarkView>> {
                    map.clear()
                    it?.let {
                        displayAllBookmarks(it)
                    }
                })
    }

    private fun startBookmarkDetails(bookmarkId: Long) {
        val intent = Intent(this, BookmarkDetailsActivity::class.java)
        intent.putExtra(EXTRA_BOOKMARK_ID, bookmarkId)
        startActivity(intent)
    }

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
        const val EXTRA_BOOKMARK_ID = "com.raywenderlich.placebook.EXTRA_BOOKMARK_ID"
    }

    class PlaceInfo(val place: Place? = null, val image: Bitmap? = null)
}
