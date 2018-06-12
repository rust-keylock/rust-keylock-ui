// Copyright 2017 astonbitecode
// This file is part of rust-keylock password manager.
//
// rust-keylock is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// rust-keylock is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with rust-keylock.  If not, see <http://www.gnu.org/licenses/>.
use log4rs;
use std::{fmt, result, io};
use std::error::Error;
use log;

pub type Result<T> = result::Result<T, RklUiError>;

#[derive(Debug)]
pub struct RklUiError {
    description: String
}

impl fmt::Display for RklUiError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.description)
    }
}

impl Error for RklUiError {
    fn description(&self) -> &str {
        self.description.as_str()
    }
}

impl From<log4rs::config::Errors> for RklUiError {
    fn from(err: log4rs::config::Errors) -> RklUiError {
        RklUiError { description: format!("{:?}", err) }
    }
}

impl From<log::SetLoggerError> for RklUiError {
    fn from(err: log::SetLoggerError) -> RklUiError {
        RklUiError { description: format!("{:?}", err) }
    }
}

impl From<io::Error> for RklUiError {
    fn from(err: io::Error) -> RklUiError {
        RklUiError { description: format!("{:?}", err) }
    }
}
