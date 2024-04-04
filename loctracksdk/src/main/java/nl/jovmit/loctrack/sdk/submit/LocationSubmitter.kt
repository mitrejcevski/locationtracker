package nl.jovmit.loctrack.sdk.submit

import nl.jovmit.loctrack.sdk.request.Location

/**
 * Submitter interface used to submit location data to an external system, depending
 * on its implementation.
 */
interface LocationSubmitter {

    suspend fun submitLocation(location: Location): LocationSubmitResult
}