import com.github.stijndehaes.playprometheusfilters.filters.{LatencyFilter, StatusCounterFilter}
import com.google.inject.Inject
import play.api.http.DefaultHttpFilters

class Filters @Inject()(
   latencyFilter: LatencyFilter,
   statusCounterFilter: StatusCounterFilter,
 ) extends DefaultHttpFilters(latencyFilter, statusCounterFilter)