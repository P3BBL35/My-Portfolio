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

    Set<TimeRange> unavailableTime = determineUnavailableTime(events, request);
    Iterator<TimeRange> iterator = unavailableTime.iterator();
    TimeRange currentEvent = (iterator.hasNext()) ?  iterator.next() : null;
    if (currentEvent != null) {
      if (TimeRange.START_OF_DAY < currentEvent.start()
          && currentEvent.start() - TimeRange.START_OF_DAY >= request.getDuration()) {
        validTimeRanges.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, currentEvent.start(), false));
      }

      while (iterator.hasNext()) {
        TimeRange nextEvent = iterator.next();

        if (nextEvent.start() - currentEvent.end() >= request.getDuration()) {
          validTimeRanges.add(TimeRange.fromStartEnd(currentEvent.end(), nextEvent.start(), false));
        }
        currentEvent = nextEvent;
      }

      if (currentEvent.end() < TimeRange.END_OF_DAY
          && TimeRange.END_OF_DAY - currentEvent.end() + 1 >= request.getDuration()) {
        validTimeRanges.add(TimeRange.fromStartEnd(currentEvent.end(), TimeRange.END_OF_DAY, true));
      }
    } else {  // There are no times taken in the day.
      validTimeRanges.add(TimeRange.WHOLE_DAY);
    }
    return validTimeRanges;
  }

  /**
   * @return a Set containing all the unavailable time in the day.
   */
  private Set<TimeRange> determineUnavailableTime(Collection<Event> events, MeetingRequest request) {
    Collection<String> attendees = request.getAttendees();
    TreeSet<TimeRange> timeTaken = new TreeSet<>((range1, range2) -> {
      if (range1.start() < range2.start()) {
        return -1;
      } else if (range1.start() > range2.start()) {
        return 1;
      } else {
        return 0;
      }
    });

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
    TimeRange firstRange = (iterator.hasNext()) ? iterator.next() : null;

    while (firstRange != null && iterator.hasNext()) {
      TimeRange toCombine = iterator.next();
      if (firstRange.overlaps(toCombine) || firstRange.end() == toCombine.start()) {
        timeRanges.remove(firstRange);
        timeRanges.remove(toCombine);
        timeRanges.add(TimeRange.fromStartEnd(
            Math.min(firstRange.start(), toCombine.start()),
            Math.max(firstRange.end(), toCombine.end()),
            false));

        // Reset iterator to contain the newly combined piece.
        iterator = timeRanges.iterator();
        firstRange = iterator.next();
      } else {
        firstRange = toCombine;  // Store current range for comparing with the next.
      }
    }
    return timeRanges;
  }

}
