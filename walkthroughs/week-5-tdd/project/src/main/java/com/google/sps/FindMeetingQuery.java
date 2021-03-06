// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public final class FindMeetingQuery {

  /**
   * Given the collection of events, this method returns a collection of all time ranges
   * that are viable for the requested meeting.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> validTimeRanges = new ArrayList<>();
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return validTimeRanges;
    }
 
    return considerOptionals(
        determineAvailableTime(events, request.getAttendees(), request.getDuration()),
        events, request);
  }

  /**
   * @return a Set containing all the unavailable time in the day.
   */
  private Set<TimeRange> determineUnavailableTime(Collection<Event> events, Collection<String> attendees) {
    TreeSet<TimeRange> timeTaken = new TreeSet<>((range1, range2) -> {
      if (range1.start() < range2.start()) {
        return -1;
      } else if (range1.start() > range2.start()) {
        return 1;
      } else {
        return 0;
      }
    });

    // TODO: Can make this O(n + m) rather than O(n * m).
    for (String attendee : attendees) {
      for (Event event : events) {
        if (event.getAttendees().contains(attendee)) {
          timeTaken.add(event.getWhen());
        }
      }
    }
    return combineRanges(timeTaken);
  }

  /**
   * Combines all ranges that overlap or are back-to-back into one range.
   */
  private TreeSet<TimeRange> combineRanges(TreeSet<TimeRange> timeRanges) {
    Iterator<TimeRange> iterator = timeRanges.iterator();
    TimeRange currentRange = (iterator.hasNext()) ? iterator.next() : null;

    while (currentRange != null && iterator.hasNext()) {
      TimeRange nextRange = iterator.next();
      if (currentRange.overlaps(nextRange) || currentRange.end() == nextRange.start()) {
        timeRanges.remove(currentRange);
        timeRanges.remove(nextRange);
        timeRanges.add(TimeRange.fromStartEnd(
            Math.min(currentRange.start(), nextRange.start()),
            Math.max(currentRange.end(), nextRange.end()),
            false));

        // Reset iterator to contain the newly combined piece.
        iterator = timeRanges.iterator();
        currentRange = iterator.next();
      } else {
        currentRange = nextRange;  // Store current range for comparing with the next.
      }
    }
    return timeRanges;
  }

  /**
   * Returns a list containing all the TimeRanges that the given collection of attendees are available for given
   * a collection of their events for that day.
   */
  private List<TimeRange> determineAvailableTime(Collection<Event> events, Collection<String> attendees, long meetingDuration) {
    Iterator<TimeRange> unavailableTimeIterator = determineUnavailableTime(events, attendees).iterator();
    TimeRange currentEvent = (unavailableTimeIterator.hasNext()) ?  unavailableTimeIterator.next() : null;
    if (currentEvent == null) {  // There are no events in the day.
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    
    List<TimeRange> availableTime = new ArrayList<>();
    if (TimeRange.START_OF_DAY < currentEvent.start()
        && currentEvent.start() - TimeRange.START_OF_DAY >= meetingDuration) {
      availableTime.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, currentEvent.start(), false));
    }

    while (unavailableTimeIterator.hasNext()) {
      TimeRange nextEvent = unavailableTimeIterator.next();

      if (nextEvent.start() - currentEvent.end() >= meetingDuration) {
        availableTime.add(TimeRange.fromStartEnd(currentEvent.end(), nextEvent.start(), false));
      }
      currentEvent = nextEvent;
    }

    if (currentEvent.end() < TimeRange.END_OF_DAY
        && TimeRange.END_OF_DAY - currentEvent.end() + 1 >= meetingDuration) {
      availableTime.add(TimeRange.fromStartEnd(currentEvent.end(), TimeRange.END_OF_DAY, true));
    }
    return availableTime;
  }

  /**
   * Given the set of meeting times, this method refits the potential meeting times to fit
   * optional attendees with the schedule, if applicable.
   */
  private Collection<TimeRange> considerOptionals(
      List<TimeRange> meetingTimes, Collection<Event> events, MeetingRequest request) {
    if (request.getOptionalAttendees().isEmpty()) {
      return meetingTimes;
    }

    Collection<String> allAttendees = new HashSet<>();;
    allAttendees.addAll(request.getAttendees());
    allAttendees.addAll(request.getOptionalAttendees());

    Collection<TimeRange> totalAvailableTime = 
        determineAvailableTime(events, allAttendees, request.getDuration());
    Iterator<TimeRange> totalAvailableTimeIterator = totalAvailableTime.iterator();
    
    if (totalAvailableTimeIterator.hasNext()) {
      return totalAvailableTime;
    } else {
      if (request.getAttendees().isEmpty()) {  // Consider when there are no mandatory attendees.
        return totalAvailableTime;
      } else {
        return meetingTimes;
      }
    }
  }
}
